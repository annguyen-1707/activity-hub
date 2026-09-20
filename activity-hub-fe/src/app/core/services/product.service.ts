import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { CategoryEnum, ProductRequest, ProductResponse } from '../models/product.model';
import { Product } from '../models/order.model';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  searchProducts(
    page: number = 0,
    size: number = 10,
    keyword: string = '',
    categoryId?: string
  ): Observable<PageResponse<ProductResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    if (categoryId && categoryId.trim()) {
      params = params.set('categoryId', categoryId.trim());
    }

    return this.http
      .get<ApiResponse<PageResponse<ProductResponse>>>(`${this.baseUrl}/products`, {
        params,
      })
      .pipe(map((res) => res.result));
  }

  getProducts(): Observable<Product[]> {
    return this.searchProducts(0, 100).pipe(
      map((page) =>
        (page?.content || []).map((p) => ({
          id: p.id,
          name: p.name,
          price: p.price,
          category: p.categoryLabel || p.category,
          image: p.image || '',
          description: p.description || '',
          stock: p.quantity,
          rating: p.rate,
        }))
      )
    );
  }

  getProductById(id: string): Observable<ProductResponse> {
    return this.http
      .get<ApiResponse<ProductResponse>>(`${this.baseUrl}/products/${id}`)
      .pipe(map((res) => res.result));
  }

  createProduct(request: ProductRequest): Observable<ProductResponse> {
    return this.http
      .post<ApiResponse<ProductResponse>>(`${this.baseUrl}/products`, request)
      .pipe(map((res) => res.result));
  }

  updateProduct(id: string, request: ProductRequest): Observable<ProductResponse> {
    return this.http
      .patch<ApiResponse<ProductResponse>>(`${this.baseUrl}/products/${id}`, request)
      .pipe(map((res) => res.result));
  }

  deleteProduct(id: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/products/${id}`)
      .pipe(map((res) => res.result));
  }
}
