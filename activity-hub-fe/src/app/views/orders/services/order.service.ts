import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../../../core/models/api-response.model';
import { CartItem, OrderRequest, OrderResponse, OrderStatistics, OrderStatus } from '../../../core/models/order.model';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly CART_KEY = 'pos_cart';

  readonly cart = signal<CartItem[]>(this.getStoredCart());

  readonly totalCartQuantity = computed(() => {
    return this.cart().reduce((sum, item) => sum + item.quantity, 0);
  });

  readonly totalCartAmount = computed(() => {
    return this.cart().reduce((sum, item) => sum + item.subtotal, 0);
  });

  private getStoredCart(): CartItem[] {
    try {
      const data = localStorage.getItem(this.CART_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  saveCart(items: CartItem[]): void {
    this.cart.set(items);
    try {
      localStorage.setItem(this.CART_KEY, JSON.stringify(items));
    } catch {}
  }

  clearCart(): void {
    this.cart.set([]);
    try {
      localStorage.removeItem(this.CART_KEY);
    } catch {}
  }

  createOrder(request: OrderRequest): Observable<OrderResponse> {
    return this.http
      .post<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders`, request)
      .pipe(map((res) => res.result));
  }

  getMyOrders(
    page: number = 0,
    size: number = 10,
    keyword: string = '',
    status?: string,
    paymentMethod?: string
  ): Observable<PageResponse<OrderResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }
    if (status && status !== 'ALL') {
      params = params.set('status', status);
    }
    if (paymentMethod && paymentMethod !== 'ALL') {
      params = params.set('paymentMethod', paymentMethod);
    }

    return this.http
      .get<ApiResponse<PageResponse<OrderResponse>>>(`${this.baseUrl}/orders/me`, {
        params,
      })
      .pipe(map((res) => res.result));
  }

  getAllOrders(): Observable<OrderResponse[]> {
    return this.http
      .get<ApiResponse<OrderResponse[]>>(`${this.baseUrl}/orders`)
      .pipe(map((res) => res.result));
  }

  getOrderById(orderId: string): Observable<OrderResponse> {
    return this.http
      .get<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders/${orderId}`)
      .pipe(map((res) => res.result));
  }

  deleteOrder(orderId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/orders/${orderId}`)
      .pipe(map((res) => res.result));
  }

  searchAdminOrders(
    page: number = 0,
    size: number = 10,
    keyword: string = '',
    status?: string,
    paymentMethod?: string
  ): Observable<PageResponse<OrderResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }
    if (status && status !== 'ALL') {
      params = params.set('status', status);
    }
    if (paymentMethod && paymentMethod !== 'ALL') {
      params = params.set('paymentMethod', paymentMethod);
    }

    return this.http
      .get<ApiResponse<PageResponse<OrderResponse>>>(`${this.baseUrl}/orders/admin`, {
        params,
      })
      .pipe(map((res) => res.result));
  }

  getAdminStatistics(): Observable<OrderStatistics> {
    return this.http
      .get<ApiResponse<OrderStatistics>>(`${this.baseUrl}/orders/admin/statistics`)
      .pipe(map((res) => res.result));
  }

  approveOrder(orderId: string): Observable<OrderResponse> {
    return this.http
      .patch<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders/${orderId}/approve`, null)
      .pipe(map((res) => res.result));
  }

  doneOrder(orderId: string): Observable<OrderResponse> {
    return this.http
      .patch<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders/${orderId}/done`, null)
      .pipe(map((res) => res.result));
  }

  rejectOrder(orderId: string): Observable<OrderResponse> {
    return this.http
      .patch<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders/${orderId}/reject`, null)
      .pipe(map((res) => res.result));
  }

  cancelOrder(orderId: string): Observable<OrderResponse> {
    return this.http
      .patch<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders/${orderId}/cancel`, null)
      .pipe(map((res) => res.result));
  }

  updateOrderStatus(orderId: string, status: OrderStatus): Observable<OrderResponse> {
    if (status === 'CONFIRMED') {
      return this.approveOrder(orderId);
    }
    if (status === 'COMPLETED') {
      return this.doneOrder(orderId);
    }
    if (status === 'CANCELLED') {
      return this.rejectOrder(orderId);
    }
    const params = new HttpParams().set('status', status);
    return this.http
      .patch<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders/${orderId}/status`, null, {
        params,
      })
      .pipe(map((res) => res.result));
  }
}

