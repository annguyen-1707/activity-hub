import {inject, Injectable, signal, Signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable, switchMap} from 'rxjs';
import {ApiResponse} from '../models/api-response.model';
import {LoginRequest, LoginResponse} from '../models/auth.model';
import {User, UserResponse} from '../models/user.model';
import {map, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly currentUser = signal<UserResponse | null>(null);
  private readonly TOKEN_KEY = 'access_token';

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  getCurrentUser(): Signal<UserResponse | null> {
    return this.currentUser.asReadonly();
  }

  setUser(user: UserResponse): void {
    this.currentUser.set(user);
  }

  clearUser(): void {
    this.currentUser.set(null);
  }

  login(request : LoginRequest): Observable<UserResponse> {
    return this.http
      .post<ApiResponse<LoginResponse>>(`${this.baseUrl}/auth/token`, request)
      .pipe(
        map((response) => response.result),
        tap((result) => {
          const token = result.token || result.accessToken;
          if (token) {
            this.setToken(token);
          }
        }),
        switchMap(() => this.getUserProfile()),
        tap((user) => {
          this.currentUser.set(user);
        })
      );
  }

  logout(): void {
    this.removeToken();
    this.clearUser();
  }

  getUserProfile(): Observable<UserResponse> {
    return this.http
      .get<ApiResponse<UserResponse>>(`${this.baseUrl}/users/my-info`)
      .pipe(map((response) => response.result));
  }
}
