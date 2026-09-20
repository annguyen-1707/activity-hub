export interface CategoryRequest {
  code: string;
  name: string;
  description?: string;
  active: boolean;
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

export interface CategoryOption {
  id: string;
  code: string;
  name: string;
}
