import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { OrderRequest, OrderResponse, Product } from '../models/order.model';

const MOCK_PRODUCTS: Product[] = [
  {
    id: 'prod-01',
    name: 'iPhone 16 Pro Max 256GB',
    price: 34990000,
    category: 'Điện thoại',
    image: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=500&auto=format&fit=crop&q=60',
    description: 'Chip A18 Pro mạnh mẽ, camera 48MP, titan tự nhiên cao cấp.',
    stock: 25,
    rating: 4.9,
  },
  {
    id: 'prod-02',
    name: 'MacBook Pro 14" M3 Pro (18GB/512GB)',
    price: 49990000,
    category: 'Laptop',
    image: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=60',
    description: 'Màn hình Liquid Retina XDR 120Hz, hiệu năng đỉnh cao cho lập trình viên và đồ họa.',
    stock: 12,
    rating: 5.0,
  },
  {
    id: 'prod-03',
    name: 'Tai nghe Sony WH-1000XM5',
    price: 7990000,
    category: 'Âm thanh',
    image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60',
    description: 'Chống ồn chủ động hàng đầu thế giới, thời lượng pin 30 giờ liên tục.',
    stock: 40,
    rating: 4.8,
  },
  {
    id: 'prod-04',
    name: 'Bàn phím cơ Keychron Q1 Pro Wireless',
    price: 4290000,
    category: 'Phụ kiện',
    image: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=60',
    description: 'Khung nhôm CNC nguyên khối, kết nối Bluetooth 5.1 & Type-C, gõ cực êm.',
    stock: 18,
    rating: 4.7,
  },
  {
    id: 'prod-05',
    name: 'Chuột Logitech MX Master 3S',
    price: 2490000,
    category: 'Phụ kiện',
    image: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&auto=format&fit=crop&q=60',
    description: 'Cảm biến 8000 DPI hoạt động trên kính, cuộn MagSpeed siêu tốc, click êm ái.',
    stock: 50,
    rating: 4.9,
  },
  {
    id: 'prod-06',
    name: 'Màn hình Dell UltraSharp U2724D 27" 2K',
    price: 11490000,
    category: 'Màn hình',
    image: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500&auto=format&fit=crop&q=60',
    description: 'Tần số quét 120Hz, tấm nền IPS Black độ tương phản 2000:1, chuẩn màu đồ họa.',
    stock: 15,
    rating: 4.8,
  },
  {
    id: 'prod-07',
    name: 'Đồng hồ thông minh Apple Watch Ultra 2',
    price: 21490000,
    category: 'Đồng hồ',
    image: 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500&auto=format&fit=crop&q=60',
    description: 'Vỏ titan 49mm siêu bền, GPS kép chính xác, pin lên tới 72 giờ ở chế độ tiết kiệm pin.',
    stock: 20,
    rating: 4.9,
  },
  {
    id: 'prod-08',
    name: 'Loa Bluetooth Marshall Stanmore III',
    price: 9290000,
    category: 'Âm thanh',
    image: 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=500&auto=format&fit=crop&q=60',
    description: 'Thiết kế vintage cổ điển quý phái, chất âm mạnh mẽ lan tỏa mọi không gian.',
    stock: 10,
    rating: 4.6,
  },
];

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getMockProducts(): Product[] {
    return [...MOCK_PRODUCTS];
  }

  createOrder(request: OrderRequest): Observable<OrderResponse> {
    return this.http
      .post<ApiResponse<OrderResponse>>(`${this.baseUrl}/orders`, request)
      .pipe(map((res) => res.result));
  }

  getMyOrders(
    page: number = 0,
    size: number = 10,
    keyword: string = ''
  ): Observable<PageResponse<OrderResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
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
}

