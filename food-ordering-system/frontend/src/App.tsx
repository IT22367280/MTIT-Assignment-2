import React, { useState, useEffect } from 'react';
import {
  ShoppingCart, Utensils, X, Plus, Minus, CreditCard, ChevronRight,
  CheckCircle2, Users, Receipt, Store, Trash2, Edit, ListTree, Wallet
} from 'lucide-react';
import { api } from './api';
import type { MenuItem, Customer, FoodOrder, Payment } from './types';

interface CartItem extends MenuItem { cartQuantity: number; }
type Tab = 'STORE' | 'MENU_ADMIN' | 'CUSTOMERS' | 'ORDERS' | 'PAYMENTS';
type ModalType = 'ADD_MENU' | 'EDIT_MENU' | 'ADD_CUSTOMER' | 'EDIT_CUSTOMER' | 'ADD_ORDER' | 'EDIT_ORDER' | 'ADD_PAYMENT' | 'EDIT_PAYMENT';

export default function App() {
  const [activeTab, setActiveTab] = useState<Tab>('STORE');

  // Data from microservices
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [orders, setOrders] = useState<FoodOrder[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);

  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalType, setModalType] = useState<ModalType | null>(null);
  const [formData, setFormData] = useState<any>({});

  // Storefront state
  const [cart, setCart] = useState<CartItem[]>([]);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [checkoutStep, setCheckoutStep] = useState(0);
  const [customerForm, setCustomerForm] = useState({ fullName: '', email: '', phone: '', address: '' });
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    fetchMenu();
    fetchCustomers();
  }, []);

  useEffect(() => {
    if (activeTab === 'ORDERS') fetchOrders();
    if (activeTab === 'PAYMENTS') { fetchPayments(); fetchOrders(); } // orders needed for dropdown
  }, [activeTab]);

  const fetchMenu = async () => { try { setMenuItems(await api.getMenuItems()); } catch { } };
  const fetchCustomers = async () => { try { setCustomers(await api.getCustomers()); } catch { } };
  const fetchOrders = async () => { try { setOrders(await api.getOrders()); } catch { } };
  const fetchPayments = async () => { try { setPayments(await api.getPayments()); } catch { } };

  // ---- MENU CRUD ----
  const saveMenu = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = { ...formData, price: parseFloat(formData.price), available: formData.available === 'true' };
      if (modalType === 'ADD_MENU') await api.createMenuItem(payload);
      else await api.updateMenuItem(formData.id, payload);
      setIsModalOpen(false); fetchMenu();
    } catch { alert('Failed to save menu item'); }
  };
  const deleteMenu = async (id: number) => {
    if (confirm('Delete this menu item?')) { await api.deleteMenuItem(id).catch(() => alert('Error')); fetchMenu(); }
  };

  // ---- CUSTOMER CRUD ----
  const saveCustomer = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (modalType === 'ADD_CUSTOMER') await api.createCustomer(formData);
      else await api.updateCustomer(formData.id, formData);
      setIsModalOpen(false); fetchCustomers();
    } catch { alert('Failed to save customer'); }
  };
  const deleteCustomer = async (id: number) => {
    if (confirm('Delete this customer?')) { await api.deleteCustomer(id).catch(() => alert('Error')); fetchCustomers(); }
  };

  // ---- ORDER CRUD ----
  const saveOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (modalType === 'ADD_ORDER') {
        const order = await api.createOrder({
          customerId: Number(formData.customerId),
          itemName: formData.itemName,
          quantity: Number(formData.quantity),
          totalAmount: parseFloat(formData.totalAmount),
          status: 'PENDING'
        });
        // Auto-create payment for new manual order
        await api.createPayment({ orderId: order.id, amount: parseFloat(formData.totalAmount), paymentMethod: 'CASH', paymentStatus: 'ON_DELIVERY' }).catch(() => {});
      } else {
        await api.updateOrderStatus(formData.id, formData.status);
      }
      setIsModalOpen(false); fetchOrders();
    } catch { alert('Failed to save order'); }
  };
  const deleteOrder = async (id: number) => {
    if (confirm('Delete this order?')) { await api.deleteOrder(id).catch(() => alert('Error')); fetchOrders(); }
  };

  // ---- PAYMENT CRUD ----
  const savePayment = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (modalType === 'ADD_PAYMENT') {
        await api.createPayment({ orderId: Number(formData.orderId), amount: parseFloat(formData.amount), paymentMethod: formData.paymentMethod, paymentStatus: formData.paymentStatus });
      } else {
        await api.updatePaymentStatus(formData.id, formData.paymentStatus);
      }
      setIsModalOpen(false); fetchPayments(); fetchOrders();
    } catch { alert('Failed to save payment'); }
  };
  const deletePayment = async (id: number) => {
    if (confirm('Delete this payment?')) { await api.deletePayment(id).catch(() => alert('Error')); fetchPayments(); fetchOrders(); }
  };

  // ---- STOREFRONT ----
  const addToCart = (item: MenuItem) => {
    setCart(prev => {
      const exist = prev.find(p => p.id === item.id);
      return exist ? prev.map(p => p.id === item.id ? { ...p, cartQuantity: p.cartQuantity + 1 } : p) : [...prev, { ...item, cartQuantity: 1 }];
    });
  };
  const updateQuantity = (id: number, delta: number) => setCart(cart.map(p => p.id === id ? { ...p, cartQuantity: Math.max(1, p.cartQuantity + delta) } : p));
  const cartTotal = cart.reduce((sum, item) => sum + item.price * item.cartQuantity, 0);
  const resolvePaymentStatus = (method: string) => method === 'CARD' ? 'PAID' : 'ON_DELIVERY';

  const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault();
    if (checkoutStep === 1) return setCheckoutStep(2);
    if (checkoutStep === 2) {
      try {
        setIsLoading(true);
        const customer = await api.createCustomer(customerForm);
        await Promise.all(cart.map(async (ci) => {
          const order = await api.createOrder({ customerId: customer.id, itemName: ci.itemName, quantity: ci.cartQuantity, totalAmount: ci.price * ci.cartQuantity, status: 'PENDING' });
          await api.createPayment({ orderId: order.id, amount: ci.price * ci.cartQuantity, paymentMethod, paymentStatus: resolvePaymentStatus(paymentMethod) });
        }));
        setCheckoutStep(3); setCart([]);
      } catch { alert('Checkout failed. Are all backend services running?'); } finally { setIsLoading(false); }
    }
  };

  // Helper: get customer name from loaded customers
  const getCustomerName = (id: number) => customers.find(c => c.id === id)?.fullName || `#${id}`;

  // Helper: table action buttons
  const ActionBtns = ({ onEdit, onDelete }: { onEdit: () => void; onDelete: () => void }) => (
    <div className="flex gap-2">
      <button className="btn btn-secondary" style={{ padding: '0.4rem', borderRadius: '6px' }} onClick={onEdit}><Edit size={15} className="text-primary" /></button>
      <button className="btn" style={{ padding: '0.4rem', borderRadius: '6px', background: '#fee2e2', color: '#dc2626', border: 'none' }} onClick={onDelete}><Trash2 size={15} /></button>
    </div>
  );

  const openAddModal = (type: ModalType, defaults: any = {}) => { setModalType(type); setFormData(defaults); setIsModalOpen(true); };
  const openEditModal = (type: ModalType, data: any) => { setModalType(type); setFormData({ ...data }); setIsModalOpen(true); };

  // -------------------------------------------------
  return (
    <div className="app-container">
      {/* ========== HEADER ========== */}
      <header className="header" style={{ flexWrap: 'wrap', gap: '0.75rem' }}>
        <div className="logo" onClick={() => setActiveTab('STORE')} style={{ cursor: 'pointer' }}>
          <Utensils size={22} />
          <span>FreshBites</span>
        </div>

        <nav style={{ display: 'flex', gap: '0.5rem', flex: 1, justifyContent: 'center', flexWrap: 'wrap' }}>
          {([
            { tab: 'STORE', icon: <Store size={16} />, label: 'Storefront' },
            { tab: 'MENU_ADMIN', icon: <ListTree size={16} />, label: 'Menu Admin' },
            { tab: 'CUSTOMERS', icon: <Users size={16} />, label: 'Customers' },
            { tab: 'ORDERS', icon: <Receipt size={16} />, label: 'Orders' },
            { tab: 'PAYMENTS', icon: <Wallet size={16} />, label: 'Payments' },
          ] as { tab: Tab; icon: React.ReactNode; label: string }[]).map(({ tab, icon, label }) => (
            <button key={tab} className={`btn ${activeTab === tab ? 'btn-primary' : 'btn-secondary'}`} style={{ border: 'none', padding: '0.6rem 1rem' }} onClick={() => setActiveTab(tab)}>
              {icon} {label}
            </button>
          ))}
        </nav>

        {activeTab === 'STORE' && (
          <button className="btn btn-secondary" onClick={() => setIsCartOpen(true)}>
            <ShoppingCart size={18} /> Cart ({cart.reduce((a, b) => a + b.cartQuantity, 0)})
          </button>
        )}
      </header>

      {/* ========== MAIN ========== */}
      <main className="main-content">

        {/* ---------- STOREFRONT ---------- */}
        {activeTab === 'STORE' && (
          <>
            <div className="hero animate-fade-in">
              <h1>Delicious Food, Delivered Fast</h1>
              <p>Fresh meals crafted by top chefs, right to your door.</p>
            </div>
            <h2 className="mb-8 flex items-center gap-2">
              <span>Our Menu</span>
              <div style={{ flex: 1, height: '1px', background: 'var(--border)', marginLeft: '1rem' }} />
            </h2>
            {menuItems.length === 0 ? (
              <div className="text-center" style={{ padding: '3rem', color: 'var(--text-muted)' }}>
                <Utensils size={48} style={{ margin: '0 auto 1rem', opacity: 0.4 }} />
                <p>No items yet. Start backend services or add items in Menu Admin.</p>
              </div>
            ) : (
              <div className="grid grid-cols-3">
                {menuItems.map((item, i) => (
                  <div key={item.id} className="card animate-fade-in" style={{ animationDelay: `${i * 0.07}s` }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <span className={`badge ${item.available ? 'badge-success' : 'badge-warning'} mb-4`} style={{ display: 'inline-block' }}>{item.available ? 'Available' : 'Sold Out'}</span>
                        <h3>{item.itemName}</h3>
                        <p className="mt-4" style={{ fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '1px' }}>{item.category}</p>
                      </div>
                      <div className="price-tag">${item.price.toFixed(2)}</div>
                    </div>
                    <button className="btn btn-primary" style={{ width: '100%', marginTop: '1.5rem' }} onClick={() => addToCart(item)} disabled={!item.available}>
                      <Plus size={16} /> Add to Order
                    </button>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* ---------- MENU ADMIN ---------- */}
        {activeTab === 'MENU_ADMIN' && (
          <div className="animate-fade-in">
            <div className="flex justify-between items-center mb-8">
              <h2>Menu Database</h2>
              <button className="btn btn-primary" onClick={() => openAddModal('ADD_MENU', { available: 'true' })}><Plus size={16} /> Add Item</button>
            </div>
            <AdminTable
              headers={['ID', 'Item Name', 'Category', 'Price', 'Availability', 'Actions']}
              empty={menuItems.length === 0}
              emptyMsg="No menu items. Add one!"
            >
              {menuItems.map(m => (
                <tr key={m.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={td}>#{m.id}</td>
                  <td style={{ ...td, fontWeight: 600 }}>{m.itemName}</td>
                  <td style={td}>{m.category}</td>
                  <td style={td}>${m.price.toFixed(2)}</td>
                  <td style={td}><span className={`badge ${m.available ? 'badge-success' : 'badge-warning'}`}>{m.available ? 'Available' : 'Sold Out'}</span></td>
                  <td style={td}><ActionBtns onEdit={() => openEditModal('EDIT_MENU', { ...m, available: m.available.toString() })} onDelete={() => deleteMenu(m.id)} /></td>
                </tr>
              ))}
            </AdminTable>
          </div>
        )}

        {/* ---------- CUSTOMERS ---------- */}
        {activeTab === 'CUSTOMERS' && (
          <div className="animate-fade-in">
            <div className="flex justify-between items-center mb-8">
              <h2>Registered Customers</h2>
              <button className="btn btn-primary" onClick={() => openAddModal('ADD_CUSTOMER')}><Plus size={16} /> Add Customer</button>
            </div>
            <AdminTable
              headers={['ID', 'Full Name', 'Email', 'Phone', 'Address', 'Actions']}
              empty={customers.length === 0}
              emptyMsg="No customers yet."
            >
              {customers.map(c => (
                <tr key={c.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={td}>#{c.id}</td>
                  <td style={{ ...td, fontWeight: 600 }}>{c.fullName}</td>
                  <td style={td}>{c.email}</td>
                  <td style={td}>{c.phone}</td>
                  <td style={td}>{c.address}</td>
                  <td style={td}><ActionBtns onEdit={() => openEditModal('EDIT_CUSTOMER', c)} onDelete={() => deleteCustomer(c.id)} /></td>
                </tr>
              ))}
            </AdminTable>
          </div>
        )}

        {/* ---------- ORDERS ---------- */}
        {activeTab === 'ORDERS' && (
          <div className="animate-fade-in">
            <div className="flex justify-between items-center mb-8">
              <h2>Food Orders</h2>
              <button className="btn btn-primary" onClick={() => openAddModal('ADD_ORDER', { status: 'PENDING' })}><Plus size={16} /> Create Order</button>
            </div>
            <AdminTable
              headers={['Order ID', 'Customer', 'Item', 'Qty', 'Total', 'Payment', 'Status', 'Actions']}
              empty={orders.length === 0}
              emptyMsg="No orders yet. Place one from the Storefront!"
            >
              {orders.map(o => (
                <tr key={o.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ ...td, fontWeight: 600 }}>#{o.id}</td>
                  <td style={td}>{o.customerName || getCustomerName(o.customerId)} <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>(#{o.customerId})</span></td>
                  <td style={td}>{o.itemName}</td>
                  <td style={td}>x{o.quantity}</td>
                  <td style={{ ...td, fontWeight: 700 }}>${o.totalAmount.toFixed(2)}</td>
                  <td style={td}>
                    {o.paymentId ? (
                      <span className="badge badge-success">#{o.paymentId}</span>
                    ) : (
                      <span style={{ color: 'var(--text-muted)' }}>Not linked</span>
                    )}
                  </td>
                  <td style={td}><span className={`badge ${o.status === 'COMPLETE' ? 'badge-success' : o.status === 'CANCELLED' ? '' : 'badge-warning'}`} style={o.status === 'CANCELLED' ? { background: '#fee2e2', color: '#dc2626' } : {}}>{o.status}</span></td>
                  <td style={td}><ActionBtns onEdit={() => openEditModal('EDIT_ORDER', o)} onDelete={() => deleteOrder(o.id)} /></td>
                </tr>
              ))}
            </AdminTable>
          </div>
        )}

        {/* ---------- PAYMENTS ---------- */}
        {activeTab === 'PAYMENTS' && (
          <div className="animate-fade-in">
            <div className="flex justify-between items-center mb-8">
              <h2>Payments</h2>
              <button className="btn btn-primary" onClick={() => openAddModal('ADD_PAYMENT', { paymentMethod: 'CARD', paymentStatus: 'PAID' })}><Plus size={16} /> Add Payment</button>
            </div>
            <AdminTable
              headers={['Payment ID', 'Order ID', 'Amount', 'Method', 'Status', 'Actions']}
              empty={payments.length === 0}
              emptyMsg="No payments yet."
            >
              {payments.map(p => (
                <tr key={p.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ ...td, fontWeight: 600 }}>#{p.id}</td>
                  <td style={td}>#{p.orderId}</td>
                  <td style={{ ...td, fontWeight: 700, color: 'var(--primary)' }}>${p.amount.toFixed(2)}</td>
                  <td style={td}><span className="badge">{p.paymentMethod}</span></td>
                  <td style={td}><span className={`badge ${p.paymentStatus === 'PAID' ? 'badge-success' : 'badge-warning'}`}>{p.paymentStatus}</span></td>
                  <td style={td}><ActionBtns onEdit={() => openEditModal('EDIT_PAYMENT', p)} onDelete={() => deletePayment(p.id)} /></td>
                </tr>
              ))}
            </AdminTable>
          </div>
        )}
      </main>

      {/* ========== ADMIN MODALS ========== */}
      {isModalOpen && modalType && (
        <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: 520 }}>
            <div className="modal-header">
              <h2>
                {{ ADD_MENU: 'Add Menu Item', EDIT_MENU: 'Edit Menu Item', ADD_CUSTOMER: 'Add Customer', EDIT_CUSTOMER: 'Edit Customer', ADD_ORDER: 'Create Custom Order', EDIT_ORDER: 'Update Order Status', ADD_PAYMENT: 'Add Payment', EDIT_PAYMENT: 'Update Payment Status' }[modalType]}
              </h2>
              <button className="close-btn" onClick={() => setIsModalOpen(false)}><X size={22} /></button>
            </div>

            <form onSubmit={modalType.includes('MENU') ? saveMenu : modalType.includes('CUSTOMER') ? saveCustomer : modalType.includes('ORDER') ? saveOrder : savePayment}>

              {/* MENU FORM */}
              {modalType.includes('MENU') && (
                <>
                  <FormGroup label="Item Name"><input required className="form-control" value={formData.itemName || ''} onChange={e => setFormData({ ...formData, itemName: e.target.value })} placeholder="e.g. Grilled Chicken" /></FormGroup>
                  <FormGroup label="Category"><input required className="form-control" value={formData.category || ''} onChange={e => setFormData({ ...formData, category: e.target.value })} placeholder="e.g. Main Course" /></FormGroup>
                  <FormGroup label="Price ($)"><input type="number" step="0.01" min="0.01" required className="form-control" value={formData.price || ''} onChange={e => setFormData({ ...formData, price: e.target.value })} /></FormGroup>
                  <FormGroup label="Availability">
                    <select className="form-control" value={formData.available} onChange={e => setFormData({ ...formData, available: e.target.value })}>
                      <option value="true">Available</option>
                      <option value="false">Sold Out</option>
                    </select>
                  </FormGroup>
                </>
              )}

              {/* CUSTOMER FORM */}
              {modalType.includes('CUSTOMER') && (
                <>
                  <FormGroup label="Full Name"><input required className="form-control" value={formData.fullName || ''} onChange={e => setFormData({ ...formData, fullName: e.target.value })} placeholder="e.g. John Doe" /></FormGroup>
                  <FormGroup label="Email"><input type="email" required className="form-control" value={formData.email || ''} onChange={e => setFormData({ ...formData, email: e.target.value })} placeholder="john@example.com" /></FormGroup>
                  <FormGroup label="Phone"><input type="tel" required className="form-control" value={formData.phone || ''} onChange={e => setFormData({ ...formData, phone: e.target.value })} placeholder="+1234567890" pattern="^[+0-9\-\s()]{7,20}$" /></FormGroup>
                  <FormGroup label="Address"><textarea required className="form-control" rows={2} value={formData.address || ''} onChange={e => setFormData({ ...formData, address: e.target.value })} placeholder="123 Main St, City" /></FormGroup>
                </>
              )}

              {/* ORDER ADD FORM — dropdowns from API */}
              {modalType === 'ADD_ORDER' && (
                <>
                  <FormGroup label="Customer">
                    <select required className="form-control" value={formData.customerId || ''} onChange={e => setFormData({ ...formData, customerId: e.target.value })}>
                      <option value="">-- Select Customer --</option>
                      {customers.map(c => <option key={c.id} value={c.id}>#{c.id} — {c.fullName}</option>)}
                    </select>
                  </FormGroup>
                  <FormGroup label="Menu Item">
                    <select required className="form-control" value={formData.itemName || ''} onChange={e => {
                      const item = menuItems.find(m => m.itemName === e.target.value);
                      setFormData({ ...formData, itemName: e.target.value, totalAmount: item ? (item.price * (formData.quantity || 1)).toFixed(2) : formData.totalAmount });
                    }}>
                      <option value="">-- Select Menu Item --</option>
                      {menuItems.filter(m => m.available).map(m => <option key={m.id} value={m.itemName}>{m.itemName} (${m.price.toFixed(2)})</option>)}
                    </select>
                  </FormGroup>
                  <FormGroup label="Quantity">
                    <input type="number" min="1" required className="form-control" value={formData.quantity || ''} onChange={e => {
                      const qty = Number(e.target.value);
                      const item = menuItems.find(m => m.itemName === formData.itemName);
                      setFormData({ ...formData, quantity: qty, totalAmount: item ? (item.price * qty).toFixed(2) : formData.totalAmount });
                    }} />
                  </FormGroup>
                  <FormGroup label="Total Amount ($)">
                    <input type="number" step="0.01" min="0.01" required className="form-control" value={formData.totalAmount || ''} onChange={e => setFormData({ ...formData, totalAmount: e.target.value })} />
                  </FormGroup>
                </>
              )}

              {/* ORDER EDIT — status only */}
              {modalType === 'EDIT_ORDER' && (
                <FormGroup label="Order Status">
                  <select className="form-control" value={formData.status} onChange={e => setFormData({ ...formData, status: e.target.value })}>
                    <option value="PENDING">PENDING</option>
                    <option value="RECEIVED">RECEIVED</option>
                    <option value="COMPLETE">COMPLETE</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </FormGroup>
              )}

              {/* PAYMENT ADD FORM — order dropdown from API */}
              {modalType === 'ADD_PAYMENT' && (
                <>
                  <FormGroup label="Order">
                    <select required className="form-control" value={formData.orderId || ''} onChange={e => {
                      const ord = orders.find(o => o.id === Number(e.target.value));
                      setFormData({ ...formData, orderId: e.target.value, amount: ord ? ord.totalAmount.toFixed(2) : formData.amount });
                    }}>
                      <option value="">-- Select Order --</option>
                      {orders.filter(o => !o.paymentId).map(o => <option key={o.id} value={o.id}>#{o.id} — {o.itemName} (${o.totalAmount.toFixed(2)})</option>)}
                    </select>
                  </FormGroup>
                  <FormGroup label="Amount ($)">
                    <input type="number" step="0.01" min="0.01" required className="form-control" value={formData.amount || ''} onChange={e => setFormData({ ...formData, amount: e.target.value })} />
                  </FormGroup>
                  <FormGroup label="Payment Method">
                    <select className="form-control" value={formData.paymentMethod} onChange={e => {
                      const paymentMethod = e.target.value;
                      setFormData({ ...formData, paymentMethod, paymentStatus: resolvePaymentStatus(paymentMethod) });
                    }}>
                      <option value="CARD">CARD</option>
                      <option value="CASH">CASH</option>
                    </select>
                  </FormGroup>
                  <FormGroup label="Payment Status">
                    <select className="form-control" value={formData.paymentStatus} onChange={e => setFormData({ ...formData, paymentStatus: e.target.value })}>
                      <option value="PAID">PAID</option>
                      {formData.paymentMethod === 'CASH' && <option value="ON_DELIVERY">ON_DELIVERY</option>}
                    </select>
                  </FormGroup>
                </>
              )}

              {/* PAYMENT EDIT — status only */}
              {modalType === 'EDIT_PAYMENT' && (
                <>
                  <div style={{ background: '#f8fafc', borderRadius: 8, padding: '1rem', marginBottom: '1rem' }}>
                    <p style={{ margin: 0 }}>Payment <strong>#{formData.id}</strong> for Order <strong>#{formData.orderId}</strong> — <span className="text-primary"><strong>${formData.amount?.toFixed(2)}</strong></span></p>
                  </div>
                  <FormGroup label="Payment Status">
                    <select className="form-control" value={formData.paymentStatus} onChange={e => setFormData({ ...formData, paymentStatus: e.target.value })}>
                      <option value="PAID">PAID</option>
                      {formData.paymentMethod === 'CASH' && <option value="ON_DELIVERY">ON_DELIVERY</option>}
                    </select>
                  </FormGroup>
                </>
              )}

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1.25rem' }}>
                <CheckCircle2 size={16} /> Save Changes
              </button>
            </form>
          </div>
        </div>
      )}

      {/* ========== STOREFRONT CART MODAL ========== */}
      {isCartOpen && activeTab === 'STORE' && (
        <div className="modal-overlay" onClick={() => { if (checkoutStep === 0 || checkoutStep === 3) setIsCartOpen(false); }}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{['Your Cart', 'Delivery Details', 'Payment', 'Order Confirmed!'][checkoutStep]}</h2>
              <button className="close-btn" onClick={() => { setIsCartOpen(false); if (checkoutStep === 3) setCheckoutStep(0); }}><X size={22} /></button>
            </div>

            {checkoutStep === 0 && (
              cart.length === 0 ? (
                <div className="text-center" style={{ padding: '2rem' }}><ShoppingCart size={48} style={{ margin: '0 auto 1rem', opacity: 0.4 }} /><p>Your cart is empty.</p></div>
              ) : (
                <div>
                  {cart.map(item => (
                    <div key={item.id} className="cart-item">
                      <div><div style={{ fontWeight: 600 }}>{item.itemName}</div><div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>${item.price.toFixed(2)} each</div></div>
                      <div className="flex items-center gap-4">
                        <div className="flex items-center gap-2" style={{ background: '#f1f5f9', borderRadius: 8, padding: '0.2rem 0.4rem' }}>
                          <button className="btn" style={{ padding: '0.2rem', border: 'none', background: 'none' }} onClick={() => updateQuantity(item.id, -1)}><Minus size={14} /></button>
                          <span style={{ fontWeight: 600, minWidth: '1.5rem', textAlign: 'center' }}>{item.cartQuantity}</span>
                          <button className="btn" style={{ padding: '0.2rem', border: 'none', background: 'none' }} onClick={() => updateQuantity(item.id, 1)}><Plus size={14} /></button>
                        </div>
                        <span style={{ fontWeight: 700, width: 60, textAlign: 'right' }}>${(item.price * item.cartQuantity).toFixed(2)}</span>
                        <button className="close-btn" onClick={() => setCart(c => c.filter(p => p.id !== item.id))}><X size={16} /></button>
                      </div>
                    </div>
                  ))}
                  <div className="flex justify-between items-center" style={{ marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '2px dashed var(--border)' }}>
                    <span style={{ fontSize: '1.2rem', fontWeight: 600 }}>Total:</span>
                    <span className="price-tag">${cartTotal.toFixed(2)}</span>
                  </div>
                  <button className="btn btn-primary" style={{ width: '100%', marginTop: '1.5rem' }} onClick={() => setCheckoutStep(1)}>Proceed to Checkout <ChevronRight size={18} /></button>
                </div>
              )
            )}

            {(checkoutStep === 1 || checkoutStep === 2) && (
              <form onSubmit={handleCheckout}>
                {checkoutStep === 1 && (
                  <div className="animate-fade-in">
                    <FormGroup label="Full Name"><input required className="form-control" value={customerForm.fullName} onChange={e => setCustomerForm({ ...customerForm, fullName: e.target.value })} placeholder="John Doe" /></FormGroup>
                    <FormGroup label="Email"><input type="email" required className="form-control" value={customerForm.email} onChange={e => setCustomerForm({ ...customerForm, email: e.target.value })} placeholder="john@example.com" /></FormGroup>
                    <FormGroup label="Phone"><input type="tel" required className="form-control" value={customerForm.phone} onChange={e => setCustomerForm({ ...customerForm, phone: e.target.value })} placeholder="+1234567890" pattern="^[+0-9\-\s()]{7,20}$" /></FormGroup>
                    <FormGroup label="Delivery Address"><textarea required rows={2} className="form-control" value={customerForm.address} onChange={e => setCustomerForm({ ...customerForm, address: e.target.value })} placeholder="123 Main St, City" /></FormGroup>
                  </div>
                )}
                {checkoutStep === 2 && (
                  <div className="animate-fade-in">
                    <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 8, marginBottom: '1.5rem' }}>
                      <div className="flex justify-between mb-4"><span>Order Total:</span><strong className="text-primary">${cartTotal.toFixed(2)}</strong></div>
                      <FormGroup label="Payment Method">
                        <select className="form-control" value={paymentMethod} onChange={e => setPaymentMethod(e.target.value)}>
                          <option value="CARD">Credit / Debit Card</option>
                          <option value="CASH">Cash on Delivery</option>
                        </select>
                      </FormGroup>
                    </div>
                    {paymentMethod === 'CARD' && (
                      <div style={{ position: 'relative' }}>
                        <CreditCard size={18} style={{ position: 'absolute', top: '0.85rem', left: '1rem', color: 'var(--text-muted)' }} />
                        <input className="form-control" style={{ paddingLeft: '2.75rem' }} placeholder="4242 4242 4242 4242" defaultValue="4242 4242 4242 4242" />
                      </div>
                    )}
                  </div>
                )}
                <div className="flex gap-4" style={{ marginTop: '2rem' }}>
                  <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setCheckoutStep(s => s - 1)} disabled={isLoading}>Back</button>
                  <button type="submit" className="btn btn-primary" style={{ flex: 2 }} disabled={isLoading}>
                    {isLoading ? 'Processing...' : checkoutStep === 1 ? 'Continue to Payment' : `Pay $${cartTotal.toFixed(2)}`}
                  </button>
                </div>
              </form>
            )}

            {checkoutStep === 3 && (
              <div className="text-center animate-fade-in" style={{ padding: '2rem 0' }}>
                <CheckCircle2 size={64} className="text-primary" style={{ margin: '0 auto 1.5rem' }} />
                <h3 className="mb-4">Order Placed Successfully!</h3>
                <p className="mb-8">Your food is being prepared and will arrive shortly.</p>
                <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => { setIsCartOpen(false); setCheckoutStep(0); }}>Back to Menu</button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

// ----- Shared sub-components -----
const td: React.CSSProperties = { padding: '1rem', verticalAlign: 'middle' };

function AdminTable({ headers, children, empty, emptyMsg }: { headers: string[]; children: React.ReactNode; empty: boolean; emptyMsg: string }) {
  return (
    <div className="card" style={{ padding: 0, overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
        <thead style={{ background: '#f8fafc', borderBottom: '2px solid var(--border)' }}>
          <tr>{headers.map(h => <th key={h} style={{ padding: '1rem', fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-muted)' }}>{h}</th>)}</tr>
        </thead>
        <tbody>
          {empty ? <tr><td colSpan={headers.length} style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>{emptyMsg}</td></tr> : children}
        </tbody>
      </table>
    </div>
  );
}

function FormGroup({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="form-group">
      <label className="form-label">{label}</label>
      {children}
    </div>
  );
}
