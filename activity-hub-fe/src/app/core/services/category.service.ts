import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { CategoryRequest, CategoryResponse } from '../models/category.model';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/categories`;

  getActiveCategories(): Observable<CategoryResponse[]> {
    return this.http
      .get<ApiResponse<CategoryResponse[]>>(this.baseUrl)
      .pipe(map((res) => res.result || []));
  }

  searchCategories(
    page: number = 0,
    size: number = 10,
    keyword: string = ''
  ): Observable<PageResponse<CategoryResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    return this.http
      .get<ApiResponse<PageResponse<CategoryResponse>>>(`${this.baseUrl}/search`, {
        params,
      })
      .pipe(map((res) => res.result));
  }

  getCategoryById(id: string): Observable<CategoryResponse> {
    return this.http
      .get<ApiResponse<CategoryResponse>>(`${this.baseUrl}/${id}`)
      .pipe(map((res) => res.result));
  }

  createCategory(request: CategoryRequest): Observable<CategoryResponse> {
    return this.http
      .post<ApiResponse<CategoryResponse>>(this.baseUrl, request)
      .pipe(map((res) => res.result));
  }

  updateCategory(id: string, request: CategoryRequest): Observable<CategoryResponse> {
    return this.http
      .put<ApiResponse<CategoryResponse>>(`${this.baseUrl}/${id}`, request)
      .pipe(map((res) => res.result));
  }

  deleteCategory(id: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map((res) => res.result));
  }
}
