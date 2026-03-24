import React, { useState, useEffect } from 'react';
import { ShoppingCart, Utensils, X, Plus, Minus, CreditCard, ChevronRight, CheckCircle2 } from 'lucide-react';
import { api } from './api';
import { MenuItem, Customer } from './types';

// Interfaces for UI state
interface CartItem extends MenuItem {
  cartQuantity: number;
}

export default function App() {
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  
  // Checkout states
  const [checkoutStep, setCheckoutStep] = useState(0); // 0: Cart, 1: Details, 2: Payment, 3: Success
  const [customerForm, setCustomerForm] = useState({ fullName: '', email: '', phone: '', address: '' });
  const [paymentMethod, setPaymentMethod] = useState('CARD');

  // Fetch Menu on load
  useEffect(() => {
    fetchMenu();
  }, []);

  const fetchMenu = async () => {
    try {
      const items = await api.getMenuItems();
      setMenuItems(items);
    } catch (error) {
      console.error('Failed to load menu:', error);
    }
  };

  const addToCart = (item: MenuItem) => {
    setCart((prev) => {
      const existing = prev.find((p) => p.id === item.id);
      if (existing) {
        return prev.map((p) => p.id === item.id ? { ...p, cartQuantity: p.cartQuantity + 1 } : p);
      }
      return [...prev, { ...item, cartQuantity: 1 }];
    });
  };

  const removeFromCart = (id: number) => {
    setCart((prev) => prev.filter((p) => p.id !== id));
  };

  const updateQuantity = (id: number, delta: number) => {
    setCart((prev) => prev.map((p) => {
      if (p.id === id) {
        const newQty = Math.max(1, p.cartQuantity + delta);
        return { ...p, cartQuantity: newQty };
      }
      return p;
    }));
  };

  const cartTotal = cart.reduce((sum, item) => sum + item.price * item.cartQuantity, 0);

  const handleCheckoutProcess = async (e: React.FormEvent) => {
    e.preventDefault();
    if (checkoutStep === 1) {
      setCheckoutStep(2);
      return;
    }
    
    if (checkoutStep === 2) {
      try {
        setIsLoading(true);
        // 1. Create Customer
        const customer = await api.createCustomer(customerForm);
        
        // 2 & 3. Create Orders and Payments for each cart item
        await Promise.all(cart.map(async (cartItem) => {
          const order = await api.createOrder({
            customerId: customer.id,
            itemName: cartItem.itemName,
            quantity: cartItem.cartQuantity,
            totalAmount: cartItem.price * cartItem.cartQuantity,
            status: 'PENDING'
          });

          await api.createPayment({
            orderId: order.id,
            amount: cartItem.price * cartItem.cartQuantity,
            paymentMethod: paymentMethod,
            paymentStatus: 'PAID'
          });
        }));

        setCheckoutStep(3);
        setCart([]);
      } catch (error) {
        console.error('Checkout failed:', error);
        alert('Checkout failed. Make sure all backend services are running.');
      } finally {
        setIsLoading(false);
      }
    }
  };

  return (
    <div className="app-container">
      {/* Header */}
      <header className="header">
        <div className="logo">
          <Utensils className="text-primary" />
          <span>FreshBites</span>
        </div>
        <button className="btn btn-secondary" onClick={() => setIsCartOpen(true)}>
          <ShoppingCart size={20} />
          Cart ({cart.reduce((a, b) => a + b.cartQuantity, 0)})
        </button>
      </header>

      {/* Main Content */}
      <main className="main-content">
        <div className="hero animate-fade-in">
          <h1>Delicious Food, Delivered Fast</h1>
          <p>Order from our curated menu of fresh, high-quality meals prepared by top chefs.</p>
        </div>

        <h2 className="mb-8 flex items-center gap-2">
          <span>Our Menu</span>
          <div style={{flex: 1, height: '1px', background: 'var(--border)', marginLeft: '1rem'}}></div>
        </h2>
        
        {menuItems.length === 0 ? (
          <div className="text-center" style={{padding: '3rem', color: 'var(--text-muted)'}}>
            <Utensils size={48} style={{margin: '0 auto 1rem', opacity: 0.5}} />
            <p>Loading menu items or no items available...</p>
            <p style={{fontSize: '0.9rem', marginTop: '0.5rem'}}>Please make sure your backend microservices are up and running.</p>
          </div>
        ) : (
          <div className="grid grid-cols-3">
            {menuItems.map((item, idx) => (
              <div key={item.id} className="card animate-fade-in" style={{animationDelay: `${idx * 0.1}s`}}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start'}}>
                  <div>
                    <span className={`badge ${item.available ? 'badge-success' : 'badge-warning'} mb-4`} style={{display: 'inline-block'}}>
                      {item.available ? 'Available' : 'Sold Out'}
                    </span>
                    <h3>{item.itemName}</h3>
                    <p className="mt-4" style={{fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '1px'}}>{item.category}</p>
                  </div>
                  <div className="price-tag">${item.price.toFixed(2)}</div>
                </div>
                
                <button 
                  className="btn btn-primary" 
                  style={{width: '100%', marginTop: '1.5rem'}}
                  onClick={() => addToCart(item)}
                  disabled={!item.available}
                >
                  <Plus size={18} /> Add to Order
                </button>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Cart & Checkout Modal */}
      {isCartOpen && (
        <div className="modal-overlay" onClick={() => { if(checkoutStep===0 || checkoutStep===3) setIsCartOpen(false); }}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>
                {checkoutStep === 0 && 'Your Cart'}
                {checkoutStep === 1 && 'Delivery Details'}
                {checkoutStep === 2 && 'Payment'}
                {checkoutStep === 3 && 'Order Confirmed!'}
              </h2>
              <button className="close-btn" onClick={() => { setIsCartOpen(false); if(checkoutStep===3) setCheckoutStep(0); }}>
                <X size={24} />
              </button>
            </div>

            {checkoutStep === 0 && (
              <>
                {cart.length === 0 ? (
                  <div className="text-center" style={{padding: '2rem 0'}}>
                    <ShoppingCart size={48} className="text-primary" style={{margin: '0 auto 1rem', opacity: 0.5}} />
                    <p>Your cart is empty.</p>
                  </div>
                ) : (
                  <div>
                    {cart.map(item => (
                      <div key={item.id} className="cart-item">
                        <div>
                          <div style={{fontWeight: 600}}>{item.itemName}</div>
                          <div style={{fontSize: '0.85rem', color: 'var(--text-muted)'}}>${item.price.toFixed(2)} each</div>
                        </div>
                        <div className="flex items-center gap-4">
                          <div className="flex items-center gap-2" style={{background: '#f1f5f9', borderRadius: '8px', padding: '0.25rem'}}>
                            <button className="btn btn-secondary" style={{padding: '0.25rem', border: 'none'}} onClick={() => updateQuantity(item.id, -1)}><Minus size={14} /></button>
                            <span style={{fontWeight: 600, minWidth: '1.5rem', textAlign: 'center'}}>{item.cartQuantity}</span>
                            <button className="btn btn-secondary" style={{padding: '0.25rem', border: 'none'}} onClick={() => updateQuantity(item.id, 1)}><Plus size={14} /></button>
                          </div>
                          <div style={{fontWeight: 700, width: '60px', textAlign: 'right'}}>${(item.price * item.cartQuantity).toFixed(2)}</div>
                          <button className="close-btn" onClick={() => removeFromCart(item.id)}><X size={18} /></button>
                        </div>
                      </div>
                    ))}
                    <div className="flex justify-between items-center" style={{marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '2px dashed var(--border)'}}>
                      <span style={{fontSize: '1.25rem', fontWeight: 600}}>Total:</span>
                      <span className="price-tag">${cartTotal.toFixed(2)}</span>
                    </div>
                    <button className="btn btn-primary" style={{width: '100%', marginTop: '1.5rem'}} onClick={() => setCheckoutStep(1)}>
                      Proceed to Checkout <ChevronRight size={18} />
                    </button>
                  </div>
                )}
              </>
            )}

            {(checkoutStep === 1 || checkoutStep === 2) && (
              <form onSubmit={handleCheckoutProcess}>
                {checkoutStep === 1 && (
                  <div className="animate-fade-in">
                    <div className="form-group">
                      <label className="form-label">Full Name</label>
                      <input required type="text" className="form-control" value={customerForm.fullName} onChange={e => setCustomerForm({...customerForm, fullName: e.target.value})} placeholder="John Doe" />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Email</label>
                      <input required type="email" className="form-control" value={customerForm.email} onChange={e => setCustomerForm({...customerForm, email: e.target.value})} placeholder="john@example.com" />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Phone</label>
                      <input required type="tel" className="form-control" value={customerForm.phone} onChange={e => setCustomerForm({...customerForm, phone: e.target.value})} placeholder="+1234567890" pattern="^[+0-9\-\s()]{7,20}$" />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Delivery Address</label>
                      <textarea required className="form-control" value={customerForm.address} onChange={e => setCustomerForm({...customerForm, address: e.target.value})} placeholder="123 Main St, City" rows={3} />
                    </div>
                  </div>
                )}

                {checkoutStep === 2 && (
                  <div className="animate-fade-in">
                    <div style={{background: '#f8fafc', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem'}}>
                      <div className="flex justify-between mb-4"><span>Order Total:</span> <strong className="text-primary">${cartTotal.toFixed(2)}</strong></div>
                      <div className="form-group" style={{marginBottom: 0}}>
                        <label className="form-label">Payment Method</label>
                        <select className="form-control" value={paymentMethod} onChange={e => setPaymentMethod(e.target.value)}>
                          <option value="CARD">Credit/Debit Card</option>
                          <option value="CASH">Cash on Delivery</option>
                        </select>
                      </div>
                    </div>
                    
                    {paymentMethod === 'CARD' && (
                      <div className="form-group" style={{position: 'relative'}}>
                        <CreditCard size={20} style={{position: 'absolute', top: '2.5rem', left: '1rem', color: 'var(--text-muted)'}} />
                        <label className="form-label">Card Details (Demo)</label>
                        <input type="text" className="form-control" style={{paddingLeft: '3rem'}} placeholder="**** **** **** ****" defaultValue="4242 4242 4242 4242" />
                      </div>
                    )}
                  </div>
                )}

                <div className="flex gap-4" style={{marginTop: '2rem'}}>
                  <button type="button" className="btn btn-secondary" style={{flex: 1}} onClick={() => setCheckoutStep(step => step - 1)} disabled={isLoading}>
                    Back
                  </button>
                  <button type="submit" className="btn btn-primary" style={{flex: 2}} disabled={isLoading}>
                    {isLoading ? 'Processing...' : (checkoutStep === 1 ? 'Continue to Payment' : `Pay $${cartTotal.toFixed(2)}`)}
                  </button>
                </div>
              </form>
            )}

            {checkoutStep === 3 && (
              <div className="text-center animate-fade-in" style={{padding: '2rem 0'}}>
                <CheckCircle2 size={64} className="text-primary" style={{margin: '0 auto 1.5rem'}} />
                <h3 className="mb-4">Order Placed Successfully!</h3>
                <p className="mb-8">Your food is being prepared and will be delivered shortly.</p>
                <button className="btn btn-primary" style={{width: '100%'}} onClick={() => { setIsCartOpen(false); setCheckoutStep(0); }}>
                  Back to Menu
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
