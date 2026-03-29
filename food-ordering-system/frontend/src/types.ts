export interface MenuItem {
  id: number;
  itemName: string;
  category: string;
  price: number;
  available: boolean;
}

export interface Customer {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  address: string;
}

export interface FoodOrder {
  id: number;
  customerId: number;
  customerName?: string;
  itemName: string;
  quantity: number;
  totalAmount: number;
  paymentId?: number | null;
  status: string;
}

export interface Payment {
  id: number;
  orderId: number;
  amount: number;
  paymentMethod: string;
  paymentStatus: string;
}
