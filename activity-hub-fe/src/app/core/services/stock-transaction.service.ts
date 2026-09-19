import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  StockTransactionRequest,
  StockTransactionResponse,
  StockTransactionType,
} from '../models/stock-transaction.model';

@Injectable({
  providedIn: 'root',
})
export class StockTransactionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  searchTransactions(
    page: number = 0,
    size: number = 10,
    type?: StockTransactionType,
    productId?: string
  ): Observable<PageResponse<StockTransactionResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');

    if (type) {
      params = params.set('type', type);
    }

    if (productId && productId.trim()) {
      params = params.set('productId', productId.trim());
    }

    return this.http
      .get<ApiResponse<PageResponse<StockTransactionResponse>>>(
        `${this.baseUrl}/stock-transactions`,
        { params }
      )
      .pipe(map((res) => res.result));
  }

  createManualTransaction(request: StockTransactionRequest): Observable<StockTransactionResponse> {
    return this.http
      .post<ApiResponse<StockTransactionResponse>>(
        `${this.baseUrl}/stock-transactions`,
        request
      )
      .pipe(map((res) => res.result));
  }
}

