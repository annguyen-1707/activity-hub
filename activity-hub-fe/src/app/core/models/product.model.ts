export interface ProductResponse {
  id: string;
  name: string;
  price: number;
  category: CategoryResponse;
  rate: number;
  totalReviews: number;
  quantity: number;
  description?: string;
  image?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductRequest {
  name: string;
  price: number;
  categoryId: string;
  description?: string;
  image?: string;
}

export interface CategoryResponse {
  id: string;
  code: string;
  name: string;
  description?: string;
  active: boolean;
  productCount?: number;
  createdAt?: string;
  updatedAt?: string;
}