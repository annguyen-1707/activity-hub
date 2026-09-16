export type PaymentMethod = 'CASH' | 'BANK';

export type OrderStatus = 'CREATED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface Product {
  id: string;
  name: string;
  price: number;
  category: string;
  image: string;
  description: string;
  stock: number;
  rating?: number;
}

export interface CartItem {
  product: Product;
  quantity: number;
  subtotal: number;
}

export interface OrderLineRequest {
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderRequest {
  paymentMethod: PaymentMethod;
  shippingAddress: string;
  note?: string;
  items: OrderLineRequest[];
}

export interface OrderLineResponse {
  id: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderResponse {
  id: string;
  userId?: string;
  totalAmount: number;
  status: OrderStatus;
  createdAt: string;
  paymentMethod: PaymentMethod;
  shippingAddress: string;
  note?: string;
  items?: OrderLineResponse[];
}

