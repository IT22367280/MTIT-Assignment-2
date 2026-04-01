import React, { useEffect, useState } from 'react';
import {
  CircleDollarSign,
  Edit,
  ListTree,
  Plus,
  Receipt,
  Trash2,
  Users,
  Wallet,
  Loader2,
  Search,
} from 'lucide-react';
import { api } from '../api';
import { DataTable, FormGroup, MetricCard, ModalShell } from '../components/common';
import type { Customer, FoodOrder, MenuItem, Payment } from '../types';
import { toast } from 'sonner';

type AdminTab = 'OVERVIEW' | 'MENU' | 'CUSTOMERS' | 'ORDERS' | 'PAYMENTS';
type ModalType =
  | 'ADD_MENU'
  | 'EDIT_MENU'
  | 'ADD_CUSTOMER'
  | 'EDIT_CUSTOMER'
  | 'ADD_ORDER'
  | 'EDIT_ORDER'
  | 'ADD_PAYMENT'
  | 'EDIT_PAYMENT';

const adminTabs: { key: AdminTab; label: string; icon: React.ReactNode }[] = [
  { key: 'OVERVIEW', label: 'Overview', icon: <CircleDollarSign size={16} /> },
  { key: 'MENU', label: 'Menu', icon: <ListTree size={16} /> },
  { key: 'CUSTOMERS', label: 'Customers', icon: <Users size={16} /> },
  { key: 'ORDERS', label: 'Orders', icon: <Receipt size={16} /> },
  { key: 'PAYMENTS', label: 'Payments', icon: <Wallet size={16} /> },
];

function resolvePaymentStatus(method: string) {
  return method === 'CARD' ? 'PAID' : 'ON_DELIVERY';
}

function ActionButtons({
  onEdit,
  onDelete,
}: {
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <div className="row-actions">
      <button type="button" className="icon-button subtle-button" onClick={onEdit} aria-label="Edit row">
        <Edit size={15} />
      </button>
      <button type="button" className="icon-button danger-button" onClick={onDelete} aria-label="Delete row">
        <Trash2 size={15} />
      </button>
    </div>
  );
}

export function AdminPage() {
  const [activeTab, setActiveTab] = useState<AdminTab>('OVERVIEW');
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [orders, setOrders] = useState<FoodOrder[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [isInitialLoadError, setIsInitialLoadError] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalType, setModalType] = useState<ModalType | null>(null);
  const [formData, setFormData] = useState<Record<string, any>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [paymentStatusFilter, setPaymentStatusFilter] = useState('');

  const refreshAll = async () => {
    try {
      setIsLoading(true);
      setLoadError(null);
      const [menu, loadedCustomers, loadedOrders, loadedPayments] = await Promise.all([
        api.getMenuItems(),
        api.getCustomers(),
        api.getOrders(),
        api.getPayments(),
      ]);

      setMenuItems(menu);
      setCustomers(loadedCustomers);
      setOrders(loadedOrders);
      setPayments(loadedPayments);
    } catch {
      setLoadError('Admin console could not load one or more services. Start the backend stack and refresh.');
      if (isInitialLoad) setIsInitialLoadError(true);
    } finally {
      setIsLoading(false);
      setIsInitialLoad(false);
    }
  };

  useEffect(() => {
    void refreshAll();
  }, []);

  const openAddModal = (type: ModalType, defaults: Record<string, any> = {}) => {
    setModalType(type);
    setFormData(defaults);
    setIsModalOpen(true);
  };

  const openEditModal = (type: ModalType, data: Record<string, any>) => {
    setModalType(type);
    setFormData({ ...data });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setModalType(null);
    setFormData({});
  };

  const getCustomerName = (id: number) => customers.find((customer) => customer.id === id)?.fullName ?? `#${id}`;

  const saveMenu = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      const payload: Omit<MenuItem, 'id'> = {
        itemName: String(formData.itemName),
        category: String(formData.category),
        price: Number.parseFloat(formData.price),
        available: formData.available === 'true',
      };

      if (modalType === 'ADD_MENU') {
        await api.createMenuItem(payload);
        toast.success('Menu item added successfully!');
      } else {
        await api.updateMenuItem(formData.id, payload);
        toast.success('Menu item updated successfully!');
      }

      closeModal();
      await refreshAll();
    } catch {
      setLoadError('Menu update failed. Check the payload and backend status.');
      toast.error('Menu update failed.');
    }
  };

  const saveCustomer = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      if (modalType === 'ADD_CUSTOMER') {
        await api.createCustomer(formData as Omit<Customer, 'id'>);
        toast.success('Customer added successfully!');
      } else {
        await api.updateCustomer(formData.id, formData as Omit<Customer, 'id'>);
        toast.success('Customer updated successfully!');
      }

      closeModal();
      await refreshAll();
    } catch {
      setLoadError('Customer update failed. Existing email conflicts now need correction instead of a blind retry.');
      toast.error('Customer update failed.');
    }
  };

  const saveOrder = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      if (modalType === 'ADD_ORDER') {
        const order = await api.createOrder({
          customerId: Number(formData.customerId),
          itemName: String(formData.itemName),
          quantity: Number(formData.quantity),
          totalAmount: Number.parseFloat(formData.totalAmount),
          status: 'PENDING',
        });

        await api.createPayment({
          orderId: order.id,
          amount: Number.parseFloat(formData.totalAmount),
          paymentMethod: 'CASH',
          paymentStatus: 'ON_DELIVERY',
        });
        toast.success('Order created successfully!');
      } else {
        await api.updateOrderStatus(formData.id, formData.status);
        toast.success('Order updated successfully!');
      }

      closeModal();
      await refreshAll();
    } catch {
      setLoadError('Order update failed. Verify the downstream services are available.');
      toast.error('Order update failed.');
    }
  };

  const savePayment = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      if (modalType === 'ADD_PAYMENT') {
        await api.createPayment({
          orderId: Number(formData.orderId),
          amount: Number.parseFloat(formData.amount),
          paymentMethod: formData.paymentMethod,
          paymentStatus: formData.paymentStatus,
        });
        toast.success('Payment added successfully!');
      } else {
        await api.updatePaymentStatus(formData.id, formData.paymentStatus);
        toast.success('Payment updated successfully!');
      }

      closeModal();
      await refreshAll();
    } catch {
      setLoadError('Payment update failed. Verify order and payment services are healthy.');
      toast.error('Payment update failed.');
    }
  };

  const deleteMenu = async (id: number) => {
    if (!window.confirm('Delete this menu item?')) {
      return;
    }

    try {
      await api.deleteMenuItem(id);
      toast.success('Menu item deleted.');
      await refreshAll();
    } catch {
      setLoadError('Menu deletion failed.');
      toast.error('Menu deletion failed.');
    }
  };

  const deleteCustomer = async (id: number) => {
    if (!window.confirm('Delete this customer?')) {
      return;
    }

    try {
      await api.deleteCustomer(id);
      toast.success('Customer deleted.');
      await refreshAll();
    } catch {
      setLoadError('Customer deletion failed.');
      toast.error('Customer deletion failed.');
    }
  };

  const deleteOrder = async (id: number) => {
    if (!window.confirm('Delete this order?')) {
      return;
    }

    try {
      await api.deleteOrder(id);
      toast.success('Order deleted.');
      await refreshAll();
    } catch {
      setLoadError('Order deletion failed.');
      toast.error('Order deletion failed.');
    }
  };

  const deletePayment = async (id: number) => {
    if (!window.confirm('Delete this payment?')) {
      return;
    }

    try {
      await api.deletePayment(id);
      toast.success('Payment deleted.');
      await refreshAll();
    } catch {
      setLoadError('Payment deletion failed.');
      toast.error('Payment deletion failed.');
    }
  };

  const pendingOrders = orders.filter((order) => order.status === 'PENDING' || order.status === 'RECEIVED').length;
  const paidPayments = payments.filter((payment) => payment.paymentStatus === 'PAID').length;
  const totalRevenue = payments
    .filter((payment) => payment.paymentStatus === 'PAID')
    .reduce((sum, payment) => sum + payment.amount, 0);
  const availableMenuCount = menuItems.filter((item) => item.available).length;

  const safeSearch = searchQuery.toLowerCase();

  const categories = Array.from(new Set(menuItems.map((item) => item.category)));

  const filteredMenu = menuItems.filter((item) =>
    (categoryFilter === '' || item.category === categoryFilter) &&
    (item.itemName.toLowerCase().includes(safeSearch) ||
      item.category.toLowerCase().includes(safeSearch) ||
      String(item.id).includes(safeSearch))
  );

  const filteredCustomers = customers.filter((c) =>
    c.fullName.toLowerCase().includes(safeSearch) ||
    c.email.toLowerCase().includes(safeSearch) ||
    String(c.id).includes(safeSearch)
  );

  const filteredOrders = orders.filter((o) =>
    (statusFilter === '' || o.status === statusFilter) &&
    (o.itemName.toLowerCase().includes(safeSearch) ||
      o.status.toLowerCase().includes(safeSearch) ||
      String(o.id).includes(safeSearch) ||
      String(o.customerId).includes(safeSearch) ||
      (o.customerName && o.customerName.toLowerCase().includes(safeSearch)))
  );

  const filteredPayments = payments.filter((p) =>
    (paymentStatusFilter === '' || p.paymentStatus === paymentStatusFilter) &&
    (p.paymentMethod.toLowerCase().includes(safeSearch) ||
      p.paymentStatus.toLowerCase().includes(safeSearch) ||
      String(p.id).includes(safeSearch) ||
      String(p.orderId).includes(safeSearch))
  );

  const renderContent = () => {
    if (isInitialLoad && !isInitialLoadError) {
      return (
        <div className="surface-panel empty-panel">
          <Loader2 size={40} className="animate-spin text-primary" />
          <h3>Loading Admin Workspace...</h3>
          <p>Connecting to microservices and gathering dashboard intelligence.</p>
        </div>
      );
    }

    if (activeTab === 'OVERVIEW') {
      return (
        <div className="page-stack">
          <div className="metrics-grid">
            <MetricCard label="Available menu items" value={String(availableMenuCount)} tone="accent" icon={<ListTree size={18} />} />
            <MetricCard label="Registered customers" value={String(customers.length)} icon={<Users size={18} />} />
            <MetricCard label="Orders needing attention" value={String(pendingOrders)} tone="warning" icon={<Receipt size={18} />} />
            <MetricCard label="Paid revenue" value={`$${totalRevenue.toFixed(2)}`} icon={<Wallet size={18} />} />
          </div>

          <div className="split-grid">
            <section className="surface-panel">
              <p className="eyebrow">Operational split</p>
              <h3>Admin functions now live behind a dedicated surface.</h3>
              <p className="body-copy">
                This keeps menu maintenance and payment operations out of the customer flow while preserving the same
                gateway-backed APIs.
              </p>
            </section>

            <section className="surface-panel">
              <p className="eyebrow">Still pending</p>
              <h3>Backend authorization</h3>
              <p className="body-copy">
                Admin isolation is now explicit in the UI, but backend role enforcement still needs Spring Security or
                another auth layer to make it real.
              </p>
            </section>
          </div>

          <section className="surface-panel">
            <p className="eyebrow">Quick signals</p>
            <div className="stack-list">
              <div className="list-row">
                <span className="list-dot" />
                <span>{orders.length} total orders loaded into the console.</span>
              </div>
              <div className="list-row">
                <span className="list-dot" />
                <span>{paidPayments} payments are marked as paid.</span>
              </div>
              <div className="list-row">
                <span className="list-dot" />
                <span>{payments.length - paidPayments} payments still need follow-up or delivery settlement.</span>
              </div>
            </div>
          </section>
        </div>
      );
    }

    if (activeTab === 'MENU') {
      return (
        <div className="page-stack">
          <div className="section-header">
            <div>
              <p className="eyebrow">Admin menu</p>
              <h3>Catalog management</h3>
            </div>
            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <select
                className="form-control"
                style={{ width: 160, padding: '0.6rem 1rem' }}
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value)}
              >
                <option value="">All Categories</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
              <div style={{ position: 'relative' }}>
                <Search
                  size={16}
                  style={{
                    position: 'absolute',
                    left: 16,
                    top: '50%',
                    transform: 'translateY(-50%)',
                    opacity: 0.5,
                    color: 'var(--text-main)',
                  }}
                />
                <input
                  className="form-control"
                  style={{ width: 220, padding: '0.6rem 1rem 0.6rem 2.6rem' }}
                  placeholder="Search catalog..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => openAddModal('ADD_MENU', { available: 'true' })}
              >
                <Plus size={16} /> Add Item
              </button>
            </div>
          </div>

          <DataTable
            headers={['ID', 'Item Name', 'Category', 'Price', 'Availability', 'Actions']}
            empty={filteredMenu.length === 0}
            emptyMessage={searchQuery ? 'No models match your search.' : 'No menu items found.'}
          >
            {filteredMenu.map((item) => (
              <tr key={item.id}>
                <td>#{item.id}</td>
                <td className="strong-cell">{item.itemName}</td>
                <td>{item.category}</td>
                <td>${item.price.toFixed(2)}</td>
                <td>
                  <span className={`status-pill ${item.available ? 'status-pill-success' : 'status-pill-warning'}`}>
                    {item.available ? 'Available' : 'Sold Out'}
                  </span>
                </td>
                <td>
                  <ActionButtons
                    onEdit={() => openEditModal('EDIT_MENU', { ...item, available: item.available.toString() })}
                    onDelete={() => deleteMenu(item.id)}
                  />
                </td>
              </tr>
            ))}
          </DataTable>
        </div>
      );
    }

    if (activeTab === 'CUSTOMERS') {
      return (
        <div className="page-stack">
          <div className="section-header">
            <div>
              <p className="eyebrow">Admin customers</p>
              <h3>Customer records</h3>
            </div>
            <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
              <div style={{ position: 'relative' }}>
                <Search size={16} style={{ position: 'absolute', left: 16, top: '50%', transform: 'translateY(-50%)', opacity: 0.5, color: 'var(--text-main)' }} />
                <input className="form-control" style={{ width: 220, padding: '0.6rem 1rem 0.6rem 2.6rem' }} placeholder="Search customers..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
              </div>
              <button type="button" className="btn btn-primary" onClick={() => openAddModal('ADD_CUSTOMER')}>
                <Plus size={16} /> Add Customer
              </button>
            </div>
          </div>

          <DataTable
            headers={['ID', 'Full Name', 'Email', 'Phone', 'Address', 'Actions']}
            empty={filteredCustomers.length === 0}
            emptyMessage={searchQuery ? 'No customers match your search.' : 'No customers found.'}
          >
            {filteredCustomers.map((customer) => (
              <tr key={customer.id}>
                <td>#{customer.id}</td>
                <td className="strong-cell">{customer.fullName}</td>
                <td>{customer.email}</td>
                <td>{customer.phone}</td>
                <td>{customer.address}</td>
                <td>
                  <ActionButtons onEdit={() => openEditModal('EDIT_CUSTOMER', customer)} onDelete={() => deleteCustomer(customer.id)} />
                </td>
              </tr>
            ))}
          </DataTable>
        </div>
      );
    }

    if (activeTab === 'ORDERS') {
      return (
        <div className="page-stack">
          <div className="section-header">
            <div>
              <p className="eyebrow">Admin orders</p>
              <h3>Order operations</h3>
            </div>
            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <select
                className="form-control"
                style={{ width: 160, padding: '0.6rem 1rem' }}
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="">All Statuses</option>
                <option value="PENDING">PENDING</option>
                <option value="RECEIVED">RECEIVED</option>
                <option value="COMPLETE">COMPLETE</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
              <div style={{ position: 'relative' }}>
                <Search
                  size={16}
                  style={{
                    position: 'absolute',
                    left: 16,
                    top: '50%',
                    transform: 'translateY(-50%)',
                    opacity: 0.5,
                    color: 'var(--text-main)',
                  }}
                />
                <input
                  className="form-control"
                  style={{ width: 220, padding: '0.6rem 1rem 0.6rem 2.6rem' }}
                  placeholder="Search orders..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => openAddModal('ADD_ORDER', { status: 'PENDING', quantity: 1 })}
              >
                <Plus size={16} /> Create Order
              </button>
            </div>
          </div>

          <DataTable
            headers={['Order ID', 'Customer', 'Item', 'Qty', 'Total', 'Payment', 'Status', 'Actions']}
            empty={filteredOrders.length === 0}
            emptyMessage={searchQuery ? 'No orders match your search.' : 'No orders found.'}
          >
            {filteredOrders.map((order) => (
              <tr key={order.id}>
                <td className="strong-cell">#{order.id}</td>
                <td>
                  {order.customerName || getCustomerName(order.customerId)}
                  <div className="muted-row">#{order.customerId}</div>
                </td>
                <td>{order.itemName}</td>
                <td>x{order.quantity}</td>
                <td>${order.totalAmount.toFixed(2)}</td>
                <td>{order.paymentId ? <span className="status-pill status-pill-success">#{order.paymentId}</span> : 'Not linked'}</td>
                <td>
                  <span
                    className={`status-pill ${
                      order.status === 'COMPLETE'
                        ? 'status-pill-success'
                        : order.status === 'CANCELLED'
                          ? 'status-pill-danger'
                          : 'status-pill-warning'
                    }`}
                  >
                    {order.status}
                  </span>
                </td>
                <td>
                  <ActionButtons onEdit={() => openEditModal('EDIT_ORDER', order)} onDelete={() => deleteOrder(order.id)} />
                </td>
              </tr>
            ))}
          </DataTable>
        </div>
      );
    }

    return (
      <div className="page-stack">
        <div className="section-header">
          <div>
            <p className="eyebrow">Admin payments</p>
            <h3>Settlement tracking</h3>
          </div>
          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <select
              className="form-control"
              style={{ width: 160, padding: '0.6rem 1rem' }}
              value={paymentStatusFilter}
              onChange={(e) => setPaymentStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="PAID">PAID</option>
              <option value="ON_DELIVERY">ON_DELIVERY</option>
            </select>
            <div style={{ position: 'relative' }}>
              <Search
                size={16}
                style={{
                  position: 'absolute',
                  left: 16,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  opacity: 0.5,
                  color: 'var(--text-main)',
                }}
              />
              <input
                className="form-control"
                style={{ width: 220, padding: '0.6rem 1rem 0.6rem 2.6rem' }}
                placeholder="Search payments..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => openAddModal('ADD_PAYMENT', { paymentMethod: 'CARD', paymentStatus: 'PAID' })}
            >
              <Plus size={16} /> Add Payment
            </button>
          </div>
        </div>

        <DataTable
          headers={['Payment ID', 'Order ID', 'Amount', 'Method', 'Status', 'Actions']}
          empty={filteredPayments.length === 0}
          emptyMessage={searchQuery ? 'No payments match your search.' : 'No payments found.'}
        >
          {filteredPayments.map((payment) => (
            <tr key={payment.id}>
              <td className="strong-cell">#{payment.id}</td>
              <td>#{payment.orderId}</td>
              <td>${payment.amount.toFixed(2)}</td>
              <td>{payment.paymentMethod}</td>
              <td>
                <span
                  className={`status-pill ${
                    payment.paymentStatus === 'PAID' ? 'status-pill-success' : 'status-pill-warning'
                  }`}
                >
                  {payment.paymentStatus}
                </span>
              </td>
              <td>
                <ActionButtons
                  onEdit={() => openEditModal('EDIT_PAYMENT', payment)}
                  onDelete={() => deletePayment(payment.id)}
                />
              </td>
            </tr>
          ))}
        </DataTable>
      </div>
    );
  };

  return (
    <div className="page-stack">
      <section className="hero-panel image-hero admin-cover">
        <div className="hero-grid">
          <div className="hero-copy">
            <p className="eyebrow">Admin workspace</p>
            <h2 className="hero-title">Operations are separated from the storefront, but still use the same gateway-backed services.</h2>
            <p className="body-copy">
              This is a UI separation, not a security claim. The console makes the role boundary explicit while backend
              authorization remains a follow-up change.
            </p>
          </div>
          <div className="hero-metrics">
            <MetricCard label="Menu items" value={String(menuItems.length)} tone="accent" icon={<ListTree size={18} />} />
            <MetricCard label="Customers" value={String(customers.length)} icon={<Users size={18} />} />
            <MetricCard label="Orders" value={String(orders.length)} icon={<Receipt size={18} />} />
            <MetricCard label="Payments" value={String(payments.length)} icon={<Wallet size={18} />} />
          </div>
        </div>
      </section>

      <section className="surface-panel page-stack">
        <div className="section-header">
          <div>
            <p className="eyebrow">Console tabs</p>
            <h3>Admin surface</h3>
          </div>
          <button type="button" className="btn btn-secondary" onClick={() => void refreshAll()} disabled={isLoading}>
            Refresh Data
          </button>
        </div>

        <div className="tab-strip">
          {adminTabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              className={`tab-button${activeTab === tab.key ? ' tab-button-active' : ''}`}
              onClick={() => {
                setActiveTab(tab.key);
                setSearchQuery('');
                setCategoryFilter('');
                setStatusFilter('');
                setPaymentStatusFilter('');
              }}
            >
              {tab.icon}
              {tab.label}
            </button>
          ))}
        </div>

        {loadError ? <div className="status-banner status-banner-warning">{loadError}</div> : null}
        {isLoading ? <div className="status-banner">Refreshing admin data...</div> : null}
        {renderContent()}
      </section>

      {isModalOpen && modalType ? (
        <ModalShell
          title={
            {
              ADD_MENU: 'Add Menu Item',
              EDIT_MENU: 'Edit Menu Item',
              ADD_CUSTOMER: 'Add Customer',
              EDIT_CUSTOMER: 'Edit Customer',
              ADD_ORDER: 'Create Manual Order',
              EDIT_ORDER: 'Update Order Status',
              ADD_PAYMENT: 'Add Payment',
              EDIT_PAYMENT: 'Update Payment Status',
            }[modalType]
          }
          subtitle={modalType.includes('ORDER') || modalType.includes('PAYMENT') ? 'This still uses the current service interactions.' : undefined}
          onClose={closeModal}
          maxWidth={560}
        >
          <form
            onSubmit={
              modalType.includes('MENU')
                ? saveMenu
                : modalType.includes('CUSTOMER')
                  ? saveCustomer
                  : modalType.includes('ORDER')
                    ? saveOrder
                    : savePayment
            }
          >
            {modalType.includes('MENU') && (
              <>
                <FormGroup label="Item Name">
                  <input
                    required
                    className="form-control"
                    value={formData.itemName || ''}
                    onChange={(event) => setFormData({ ...formData, itemName: event.target.value })}
                    placeholder="Grilled Chicken Bowl"
                  />
                </FormGroup>
                <FormGroup label="Category">
                  <input
                    required
                    className="form-control"
                    value={formData.category || ''}
                    onChange={(event) => setFormData({ ...formData, category: event.target.value })}
                    placeholder="Main Course"
                  />
                </FormGroup>
                <FormGroup label="Price">
                  <input
                    required
                    type="number"
                    min="0.01"
                    step="0.01"
                    className="form-control"
                    value={formData.price || ''}
                    onChange={(event) => setFormData({ ...formData, price: event.target.value })}
                  />
                </FormGroup>
                <FormGroup label="Availability">
                  <select
                    className="form-control"
                    value={formData.available}
                    onChange={(event) => setFormData({ ...formData, available: event.target.value })}
                  >
                    <option value="true">Available</option>
                    <option value="false">Sold Out</option>
                  </select>
                </FormGroup>
              </>
            )}

            {modalType.includes('CUSTOMER') && (
              <>
                <FormGroup label="Full Name">
                  <input
                    required
                    className="form-control"
                    value={formData.fullName || ''}
                    onChange={(event) => setFormData({ ...formData, fullName: event.target.value })}
                    placeholder="Jane Doe"
                  />
                </FormGroup>
                <FormGroup label="Email">
                  <input
                    required
                    type="email"
                    className="form-control"
                    value={formData.email || ''}
                    onChange={(event) => setFormData({ ...formData, email: event.target.value })}
                    placeholder="jane@example.com"
                  />
                </FormGroup>
                <FormGroup label="Phone">
                  <input
                    required
                    type="tel"
                    className="form-control"
                    value={formData.phone || ''}
                    onChange={(event) => setFormData({ ...formData, phone: event.target.value })}
                    placeholder="+94 77 123 4567"
                  />
                </FormGroup>
                <FormGroup label="Address">
                  <textarea
                    required
                    rows={3}
                    className="form-control"
                    value={formData.address || ''}
                    onChange={(event) => setFormData({ ...formData, address: event.target.value })}
                    placeholder="123 Main Street, Colombo"
                  />
                </FormGroup>
              </>
            )}

            {modalType === 'ADD_ORDER' && (
              <>
                <FormGroup label="Customer">
                  <select
                    required
                    className="form-control"
                    value={formData.customerId || ''}
                    onChange={(event) => setFormData({ ...formData, customerId: event.target.value })}
                  >
                    <option value="">Select Customer</option>
                    {customers.map((customer) => (
                      <option key={customer.id} value={customer.id}>
                        #{customer.id} - {customer.fullName}
                      </option>
                    ))}
                  </select>
                </FormGroup>
                <FormGroup label="Menu Item">
                  <select
                    required
                    className="form-control"
                    value={formData.itemName || ''}
                    onChange={(event) => {
                      const selectedItem = menuItems.find((item) => item.itemName === event.target.value);
                      setFormData({
                        ...formData,
                        itemName: event.target.value,
                        totalAmount: selectedItem
                          ? (selectedItem.price * Number(formData.quantity || 1)).toFixed(2)
                          : formData.totalAmount,
                      });
                    }}
                  >
                    <option value="">Select Item</option>
                    {menuItems
                      .filter((item) => item.available)
                      .map((item) => (
                        <option key={item.id} value={item.itemName}>
                          {item.itemName} (${item.price.toFixed(2)})
                        </option>
                      ))}
                  </select>
                </FormGroup>
                <FormGroup label="Quantity">
                  <input
                    required
                    min="1"
                    type="number"
                    className="form-control"
                    value={formData.quantity || ''}
                    onChange={(event) => {
                      const quantity = Number(event.target.value);
                      const selectedItem = menuItems.find((item) => item.itemName === formData.itemName);
                      setFormData({
                        ...formData,
                        quantity,
                        totalAmount: selectedItem ? (selectedItem.price * quantity).toFixed(2) : formData.totalAmount,
                      });
                    }}
                  />
                </FormGroup>
                <FormGroup label="Total Amount">
                  <input
                    required
                    type="number"
                    min="0.01"
                    step="0.01"
                    className="form-control"
                    value={formData.totalAmount || ''}
                    onChange={(event) => setFormData({ ...formData, totalAmount: event.target.value })}
                  />
                </FormGroup>
              </>
            )}

            {modalType === 'EDIT_ORDER' && (
              <FormGroup label="Order Status">
                <select
                  className="form-control"
                  value={formData.status}
                  onChange={(event) => setFormData({ ...formData, status: event.target.value })}
                >
                  <option value="PENDING">PENDING</option>
                  <option value="RECEIVED">RECEIVED</option>
                  <option value="COMPLETE">COMPLETE</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </FormGroup>
            )}

            {modalType === 'ADD_PAYMENT' && (
              <>
                <FormGroup label="Order">
                  <select
                    required
                    className="form-control"
                    value={formData.orderId || ''}
                    onChange={(event) => {
                      const selectedOrder = orders.find((order) => order.id === Number(event.target.value));
                      setFormData({
                        ...formData,
                        orderId: event.target.value,
                        amount: selectedOrder ? selectedOrder.totalAmount.toFixed(2) : formData.amount,
                      });
                    }}
                  >
                    <option value="">Select Order</option>
                    {orders
                      .filter((order) => !order.paymentId)
                      .map((order) => (
                        <option key={order.id} value={order.id}>
                          #{order.id} - {order.itemName} (${order.totalAmount.toFixed(2)})
                        </option>
                      ))}
                  </select>
                </FormGroup>
                <FormGroup label="Amount">
                  <input
                    required
                    type="number"
                    min="0.01"
                    step="0.01"
                    className="form-control"
                    value={formData.amount || ''}
                    onChange={(event) => setFormData({ ...formData, amount: event.target.value })}
                  />
                </FormGroup>
                <FormGroup label="Payment Method">
                  <select
                    className="form-control"
                    value={formData.paymentMethod}
                    onChange={(event) => {
                      const paymentMethod = event.target.value;
                      setFormData({
                        ...formData,
                        paymentMethod,
                        paymentStatus: resolvePaymentStatus(paymentMethod),
                      });
                    }}
                  >
                    <option value="CARD">CARD</option>
                    <option value="CASH">CASH</option>
                  </select>
                </FormGroup>
                <FormGroup label="Payment Status">
                  <select
                    className="form-control"
                    value={formData.paymentStatus}
                    onChange={(event) => setFormData({ ...formData, paymentStatus: event.target.value })}
                  >
                    <option value="PAID">PAID</option>
                    {formData.paymentMethod === 'CASH' ? <option value="ON_DELIVERY">ON_DELIVERY</option> : null}
                  </select>
                </FormGroup>
              </>
            )}

            {modalType === 'EDIT_PAYMENT' && (
              <>
                <div className="surface-inset">
                  <div className="summary-strip">
                    <span>Payment #{formData.id}</span>
                    <strong>${Number(formData.amount || 0).toFixed(2)}</strong>
                  </div>
                  <p className="body-copy">Order #{formData.orderId}</p>
                </div>
                <FormGroup label="Payment Status">
                  <select
                    className="form-control"
                    value={formData.paymentStatus}
                    onChange={(event) => setFormData({ ...formData, paymentStatus: event.target.value })}
                  >
                    <option value="PAID">PAID</option>
                    {formData.paymentMethod === 'CASH' ? <option value="ON_DELIVERY">ON_DELIVERY</option> : null}
                  </select>
                </FormGroup>
              </>
            )}

            <button type="submit" className="btn btn-primary full-width">
              Save Changes
            </button>
          </form>
        </ModalShell>
      ) : null}
    </div>
  );
}
