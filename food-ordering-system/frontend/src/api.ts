import { MenuItem, Customer, FoodOrder, Payment } from './types';

const API_BASE = '/api';

export const api = {
  // Menu endpoints
  getMenuItems: async (): Promise<MenuItem[]> => {
    const res = await fetch(`${API_BASE}/menu/menu-items`);
    if (!res.ok) throw new Error('Failed to fetch menu');
    return res.json();
  },

  // Customer endpoints
  createCustomer: async (customer: Omit<Customer, 'id'>): Promise<Customer> => {
    const res = await fetch(`${API_BASE}/customers`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customer)
    });
    if (!res.ok) throw new Error('Failed to create customer');
    return res.json();
  },

  // Order endpoints
  createOrder: async (order: Omit<FoodOrder, 'id'>): Promise<FoodOrder> => {
    const res = await fetch(`${API_BASE}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(order)
    });
    if (!res.ok) throw new Error('Failed to create order');
    return res.json();
  },

  // Payment endpoints
  createPayment: async (payment: Omit<Payment, 'id'>): Promise<Payment> => {
    const res = await fetch(`${API_BASE}/payments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payment)
    });
    if (!res.ok) throw new Error('Failed to process payment');
    return res.json();
  }
};
