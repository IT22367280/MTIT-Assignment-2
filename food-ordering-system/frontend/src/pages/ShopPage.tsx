import React, { useEffect, useState } from 'react';
import {
  CheckCircle2,
  ChevronRight,
  CreditCard,
  Minus,
  Plus,
  ShoppingCart,
  Utensils,
  X,
} from 'lucide-react';
import { api } from '../api';
import type { Customer, MenuItem } from '../types';
import { FormGroup, MetricCard, ModalShell } from '../components/common';

interface CartItem extends MenuItem {
  cartQuantity: number;
}

const initialCustomerForm: Omit<Customer, 'id'> = {
  fullName: '',
  email: '',
  phone: '',
  address: '',
};

function resolvePaymentStatus(method: string) {
  return method === 'CARD' ? 'PAID' : 'ON_DELIVERY';
}

export function ShopPage() {
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [checkoutStep, setCheckoutStep] = useState(0);
  const [customerForm, setCustomerForm] = useState(initialCustomerForm);
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    const loadMenu = async () => {
      try {
        setLoadError(null);
        setMenuItems(await api.getMenuItems());
      } catch {
        setLoadError('Menu service is unavailable. Start the backend stack and reload the shop.');
      }
    };

    void loadMenu();
  }, []);

  const availableItems = menuItems.filter((item) => item.available);
  const cartCount = cart.reduce((count, item) => count + item.cartQuantity, 0);
  const cartTotal = cart.reduce((sum, item) => sum + item.price * item.cartQuantity, 0);

  const addToCart = (item: MenuItem) => {
    setCart((previous) => {
      const existing = previous.find((entry) => entry.id === item.id);

      if (existing) {
        return previous.map((entry) =>
          entry.id === item.id ? { ...entry, cartQuantity: entry.cartQuantity + 1 } : entry,
        );
      }

      return [...previous, { ...item, cartQuantity: 1 }];
    });
  };

  const updateQuantity = (id: number, delta: number) => {
    setCart((previous) =>
      previous.map((item) =>
        item.id === id ? { ...item, cartQuantity: Math.max(1, item.cartQuantity + delta) } : item,
      ),
    );
  };

  const removeFromCart = (id: number) => {
    setCart((previous) => previous.filter((item) => item.id !== id));
  };

  const resetCheckout = () => {
    setIsCartOpen(false);
    setCheckoutStep(0);
    setCustomerForm(initialCustomerForm);
    setPaymentMethod('CARD');
  };

  const handleCheckout = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (checkoutStep === 1) {
      setCheckoutStep(2);
      return;
    }

    if (checkoutStep !== 2) {
      return;
    }

    try {
      setIsLoading(true);
      const customer = await api.resolveCustomer(customerForm);

      await Promise.all(
        cart.map(async (cartItem) => {
          const order = await api.createOrder({
            customerId: customer.id,
            itemName: cartItem.itemName,
            quantity: cartItem.cartQuantity,
            totalAmount: cartItem.price * cartItem.cartQuantity,
            status: 'PENDING',
          });

          await api.createPayment({
            orderId: order.id,
            amount: cartItem.price * cartItem.cartQuantity,
            paymentMethod,
            paymentStatus: resolvePaymentStatus(paymentMethod),
          });
        }),
      );

      setCheckoutStep(3);
      setCart([]);
      setCustomerForm(initialCustomerForm);
      setPaymentMethod('CARD');
    } catch {
      setLoadError('Checkout failed. Verify the customer, order, and payment services are all running.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="page-stack">
      <section className="hero-panel shop-hero">
        <div className="hero-grid">
          <div className="hero-copy">
            <p className="eyebrow">Customer workspace</p>
            <h2 className="hero-title">Browse, cart, and checkout without dragging admin controls into the same screen.</h2>
            <p className="body-copy">
              Checkout now resolves an existing customer by email before creating a new profile, which avoids the
              duplicate-email failure path in the original flow.
            </p>
            <div className="inline-actions">
              <button type="button" className="btn btn-primary" onClick={() => setIsCartOpen(true)}>
                <ShoppingCart size={16} />
                Open Cart ({cartCount})
              </button>
            </div>
          </div>

          <div className="hero-metrics">
            <MetricCard label="Available dishes" value={String(availableItems.length)} tone="accent" />
            <MetricCard label="Items in cart" value={String(cartCount)} />
            <MetricCard label="Order total" value={`$${cartTotal.toFixed(2)}`} />
            <MetricCard label="Checkout mode" value="Email-resolved" tone="warning" />
          </div>
        </div>
      </section>

      {loadError ? <div className="status-banner status-banner-warning">{loadError}</div> : null}

      <section className="section-header">
        <div>
          <p className="eyebrow">Menu</p>
          <h3>Current offerings</h3>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => setIsCartOpen(true)}>
          <ShoppingCart size={16} />
          Cart ({cartCount})
        </button>
      </section>

      {menuItems.length === 0 ? (
        <section className="surface-panel empty-panel">
          <Utensils size={40} />
          <h3>No menu items available</h3>
          <p>Start the backend services or add items from the admin console.</p>
        </section>
      ) : (
        <section className="catalog-grid">
          {menuItems.map((item) => (
            <article key={item.id} className={`catalog-card${item.available ? '' : ' catalog-card-muted'}`}>
              <div className="catalog-top">
                <span className={`status-pill ${item.available ? 'status-pill-success' : 'status-pill-warning'}`}>
                  {item.available ? 'Available' : 'Sold Out'}
                </span>
                <span className="price-pill">${item.price.toFixed(2)}</span>
              </div>
              <div className="catalog-body">
                <h3>{item.itemName}</h3>
                <p>{item.category}</p>
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => addToCart(item)}
                disabled={!item.available}
              >
                <Plus size={16} />
                Add to Cart
              </button>
            </article>
          ))}
        </section>
      )}

      {isCartOpen ? (
        <ModalShell
          title={['Your Cart', 'Delivery Details', 'Payment', 'Order Confirmed'][checkoutStep] ?? 'Your Cart'}
          subtitle={
            checkoutStep === 1
              ? 'Customer details are matched by email before checkout.'
              : checkoutStep === 2
                ? 'Payments still use the existing microservice flow.'
                : undefined
          }
          onClose={() => {
            if (checkoutStep === 0 || checkoutStep === 3) {
              resetCheckout();
            }
          }}
          maxWidth={560}
        >
          {checkoutStep === 0 && (
            <>
              {cart.length === 0 ? (
                <div className="empty-panel compact-empty">
                  <ShoppingCart size={38} />
                  <h3>Your cart is empty</h3>
                  <p>Add a few dishes to start the customer flow.</p>
                </div>
              ) : (
                <div className="page-stack">
                  {cart.map((item) => (
                    <div key={item.id} className="cart-row">
                      <div>
                        <strong>{item.itemName}</strong>
                        <p>${item.price.toFixed(2)} each</p>
                      </div>

                      <div className="cart-actions">
                        <div className="quantity-control">
                          <button type="button" className="icon-button subtle-button" onClick={() => updateQuantity(item.id, -1)}>
                            <Minus size={14} />
                          </button>
                          <span>{item.cartQuantity}</span>
                          <button type="button" className="icon-button subtle-button" onClick={() => updateQuantity(item.id, 1)}>
                            <Plus size={14} />
                          </button>
                        </div>
                        <strong>${(item.price * item.cartQuantity).toFixed(2)}</strong>
                        <button type="button" className="icon-button subtle-button" onClick={() => removeFromCart(item.id)}>
                          <X size={14} />
                        </button>
                      </div>
                    </div>
                  ))}

                  <div className="summary-strip">
                    <span>Total</span>
                    <strong>${cartTotal.toFixed(2)}</strong>
                  </div>

                  <button type="button" className="btn btn-primary" onClick={() => setCheckoutStep(1)}>
                    Proceed to Checkout
                    <ChevronRight size={16} />
                  </button>
                </div>
              )}
            </>
          )}

          {(checkoutStep === 1 || checkoutStep === 2) && (
            <form onSubmit={handleCheckout}>
              {checkoutStep === 1 && (
                <div className="page-stack">
                  <FormGroup label="Full Name">
                    <input
                      required
                      className="form-control"
                      value={customerForm.fullName}
                      onChange={(event) => setCustomerForm({ ...customerForm, fullName: event.target.value })}
                      placeholder="Jane Doe"
                    />
                  </FormGroup>
                  <FormGroup label="Email" hint="Used to reuse an existing customer profile when possible">
                    <input
                      required
                      type="email"
                      className="form-control"
                      value={customerForm.email}
                      onChange={(event) => setCustomerForm({ ...customerForm, email: event.target.value })}
                      placeholder="jane@example.com"
                    />
                  </FormGroup>
                  <FormGroup label="Phone">
                    <input
                      required
                      type="tel"
                      className="form-control"
                      value={customerForm.phone}
                      onChange={(event) => setCustomerForm({ ...customerForm, phone: event.target.value })}
                      placeholder="+94 77 123 4567"
                    />
                  </FormGroup>
                  <FormGroup label="Delivery Address">
                    <textarea
                      required
                      rows={3}
                      className="form-control"
                      value={customerForm.address}
                      onChange={(event) => setCustomerForm({ ...customerForm, address: event.target.value })}
                      placeholder="123 Main Street, Colombo"
                    />
                  </FormGroup>
                </div>
              )}

              {checkoutStep === 2 && (
                <div className="page-stack">
                  <div className="surface-inset">
                    <div className="summary-strip">
                      <span>Order total</span>
                      <strong>${cartTotal.toFixed(2)}</strong>
                    </div>
                    <FormGroup label="Payment Method">
                      <select
                        className="form-control"
                        value={paymentMethod}
                        onChange={(event) => setPaymentMethod(event.target.value)}
                      >
                        <option value="CARD">Credit / Debit Card</option>
                        <option value="CASH">Cash on Delivery</option>
                      </select>
                    </FormGroup>
                  </div>

                  {paymentMethod === 'CARD' && (
                    <div className="card-field">
                      <CreditCard size={18} />
                      <input
                        className="form-control card-input"
                        placeholder="4242 4242 4242 4242"
                        defaultValue="4242 4242 4242 4242"
                      />
                    </div>
                  )}
                </div>
              )}

              <div className="inline-actions stretch-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setCheckoutStep((step) => Math.max(0, step - 1))}
                  disabled={isLoading}
                >
                  Back
                </button>
                <button type="submit" className="btn btn-primary" disabled={isLoading}>
                  {isLoading ? 'Processing...' : checkoutStep === 1 ? 'Continue to Payment' : `Pay $${cartTotal.toFixed(2)}`}
                </button>
              </div>
            </form>
          )}

          {checkoutStep === 3 && (
            <div className="empty-panel compact-empty">
              <CheckCircle2 size={52} />
              <h3>Order placed successfully</h3>
              <p>Your order has been passed through the existing order and payment services.</p>
              <button type="button" className="btn btn-primary" onClick={resetCheckout}>
                Back to Menu
              </button>
            </div>
          )}
        </ModalShell>
      ) : null}
    </div>
  );
}
