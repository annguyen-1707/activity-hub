import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { Product } from '../models/order.model';

interface ProductApiResponse {
  id: string;
  name: string;
  price: number;
  category: string;
  categoryLabel: string;
  rate: number;
  quantity: number;
  description: string;
  image: string;
}

function toProduct(p: ProductApiResponse): Product {
  return {
    id: p.id,
    name: p.name,
    price: p.price,
    category: p.categoryLabel || p.category,
    image: p.image,
    description: p.description,
    stock: p.quantity,
    rating: p.rate,
  };
}

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getProducts(keyword: string = ''): Observable<Product[]> {
    let params = new HttpParams().set('page', '0').set('size', '200');
    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    return this.http
      .get<ApiResponse<PageResponse<ProductApiResponse>>>(`${this.baseUrl}/products`, { params })
      .pipe(map((res) => res.result.content.map(toProduct)));
  }
}
