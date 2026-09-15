import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  Role,
  RoleResponse,
  User,
  UserCreationRequest,
  UserResponse,
  UserUpdateRequest,
} from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getUsers(
    page: number = 0,
    size: number = 10,
    keyword: string = ''
  ): Observable<PageResponse<User>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (keyword && keyword.trim()) {
      params = params.set('keyword', keyword.trim());
    }

    return this.http
      .get<ApiResponse<PageResponse<User>>>(`${this.baseUrl}/users`, {
        params,
      })
      .pipe(map((res) => res.result));
  }

  getUserById(id: string): Observable<UserResponse> {
    return this.http
      .get<ApiResponse<UserResponse>>(`${this.baseUrl}/users/${id}`)
      .pipe(map((res) => res.result));
  }

  createUser(request: UserCreationRequest): Observable<UserResponse> {
    return this.http
      .post<ApiResponse<UserResponse>>(`${this.baseUrl}/users`, request)
      .pipe(map((res) => res.result));
  }

  updateUser(
    id: string,
    request: UserUpdateRequest
  ): Observable<UserResponse> {
    return this.http
      .patch<ApiResponse<UserResponse>>(`${this.baseUrl}/users/${id}`, request)
      .pipe(map((res) => res.result));
  }

  deleteUser(id: string): Observable<string> {
    return this.http
      .delete<ApiResponse<string>>(`${this.baseUrl}/users/${id}`)
      .pipe(map((res) => res.result));
  }

  getRoles(): Observable<RoleResponse[]> {
    return this.http
      .get<ApiResponse<RoleResponse[]>>(`${this.baseUrl}/roles`)
      .pipe(map((res) => res.result));
  }
}
