import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { ReviewRequest, ReviewResponse } from '../models/review.model';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  createReview(orderLineId: string, request: ReviewRequest): Observable<ReviewResponse> {
    return this.http
      .post<ApiResponse<ReviewResponse>>(`${this.baseUrl}/order-lines/${orderLineId}/review`, request)
      .pipe(map((res) => res.result));
  }

  updateReview(orderLineId: string, request: ReviewRequest): Observable<ReviewResponse> {
    return this.http
      .put<ApiResponse<ReviewResponse>>(`${this.baseUrl}/order-lines/${orderLineId}/review`, request)
      .pipe(map((res) => res.result));
  }

  getReviewForOrderLine(orderLineId: string): Observable<ReviewResponse | null> {
    return this.http
      .get<ApiResponse<ReviewResponse | null>>(`${this.baseUrl}/order-lines/${orderLineId}/review`)
      .pipe(map((res) => res.result));
  }

  getReviewsForOrder(orderId: string): Observable<ReviewResponse[]> {
    return this.http
      .get<ApiResponse<ReviewResponse[]>>(`${this.baseUrl}/orders/${orderId}/reviews`)
      .pipe(map((res) => res.result || []));
  }

  getReviewsForProduct(productId: string, page: number = 0, size: number = 10): Observable<PageResponse<ReviewResponse>> {
    return this.http
      .get<ApiResponse<PageResponse<ReviewResponse>>>(`${this.baseUrl}/products/${productId}/reviews`, {
        params: { page: page.toString(), size: size.toString() },
      })
      .pipe(map((res) => res.result));
  }
}
