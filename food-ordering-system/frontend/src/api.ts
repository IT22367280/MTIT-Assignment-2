import type { MenuItem, Customer, FoodOrder, Payment } from './types';

const API_BASE = '/api';

export const api = {
  // --- MENU SERVICE ---
  getMenuItems: async (): Promise<MenuItem[]> => {
    const res = await fetch(`${API_BASE}/menu/menu-items`);
    if (!res.ok) throw new Error('Failed to fetch menu');
    return res.json();
  },
  createMenuItem: async (item: Omit<MenuItem, 'id'>): Promise<MenuItem> => {
    const res = await fetch(`${API_BASE}/menu/menu-items`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(item)
    });
    if (!res.ok) throw new Error('Failed to create menu item');
    return res.json();
  },
  updateMenuItem: async (id: number, item: Omit<MenuItem, 'id'>): Promise<MenuItem> => {
    const res = await fetch(`${API_BASE}/menu/menu-items/${id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(item)
    });
    if (!res.ok) throw new Error('Failed to update menu item');
    return res.json();
  },
  deleteMenuItem: async (id: number): Promise<void> => {
    const res = await fetch(`${API_BASE}/menu/menu-items/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete menu item');
  },

  // --- CUSTOMER SERVICE ---
  getCustomers: async (): Promise<Customer[]> => {
    const res = await fetch(`${API_BASE}/customers`);
    if (!res.ok) throw new Error('Failed to fetch customers');
    return res.json();
  },
  createCustomer: async (customer: Omit<Customer, 'id'>): Promise<Customer> => {
    const res = await fetch(`${API_BASE}/customers`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(customer)
    });
    if (!res.ok) throw new Error('Failed to create customer');
    return res.json();
  },
  updateCustomer: async (id: number, customer: Omit<Customer, 'id'>): Promise<Customer> => {
    const res = await fetch(`${API_BASE}/customers/${id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(customer)
    });
    if (!res.ok) throw new Error('Failed to update customer');
    return res.json();
  },
  deleteCustomer: async (id: number): Promise<void> => {
    const res = await fetch(`${API_BASE}/customers/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete customer');
  },

  // --- ORDER SERVICE ---
  getOrders: async (): Promise<FoodOrder[]> => {
    const res = await fetch(`${API_BASE}/orders`);
    if (!res.ok) throw new Error('Failed to fetch orders');
    return res.json();
  },
  createOrder: async (order: Omit<FoodOrder, 'id' | 'customerName' | 'paymentId'>): Promise<FoodOrder> => {
    const res = await fetch(`${API_BASE}/orders`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(order)
    });
    if (!res.ok) throw new Error('Failed to create order');
    return res.json();
  },
  updateOrderStatus: async (id: number, status: string): Promise<FoodOrder> => {
    const res = await fetch(`${API_BASE}/orders/${id}/status`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status })
    });
    if (!res.ok) throw new Error('Failed to update order status');
    return res.json();
  },
  deleteOrder: async (id: number): Promise<void> => {
    const res = await fetch(`${API_BASE}/orders/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete order');
  },

  // --- PAYMENT SERVICE ---
  getPayments: async (): Promise<Payment[]> => {
    const res = await fetch(`${API_BASE}/payments`);
    if (!res.ok) throw new Error('Failed to fetch payments');
    return res.json();
  },
  createPayment: async (payment: Omit<Payment, 'id'>): Promise<Payment> => {
    const res = await fetch(`${API_BASE}/payments`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payment)
    });
    if (!res.ok) throw new Error('Failed to create payment');
    return res.json();
  },
  updatePaymentStatus: async (id: number, paymentStatus: string): Promise<Payment> => {
    const res = await fetch(`${API_BASE}/payments/${id}/status`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ paymentStatus })
    });
    if (!res.ok) throw new Error('Failed to update payment status');
    return res.json();
  },
  deletePayment: async (id: number): Promise<void> => {
    const res = await fetch(`${API_BASE}/payments/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete payment');
  },
};
