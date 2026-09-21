export interface ReviewRequest {
  rating: number;
  comment?: string;
}

export interface ReviewResponse {
  id: string;
  orderLineId: string;
  productId: string;
  productName: string;
  userId: string;
  username: string;
  rating: number;
  comment?: string;
  createdAt: string;
  updatedAt?: string;
}
