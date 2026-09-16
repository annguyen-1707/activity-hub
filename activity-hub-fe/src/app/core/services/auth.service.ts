import {inject, Injectable, signal, Signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable, switchMap} from 'rxjs';
import {ApiResponse} from '../models/api-response.model';
import {LoginRequest, LoginResponse} from '../models/auth.model';
import {UserResponse} from '../models/user.model';
import {map, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly TOKEN_KEY = 'access_token';
  private readonly USER_KEY = 'user_profile';

  private readonly currentUser = signal<UserResponse | null>(this.getStoredUser());

  private getStoredUser(): UserResponse | null {
    try {
      const stored = localStorage.getItem(this.USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }

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
    try {
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    } catch {}
  }

  clearUser(): void {
    this.currentUser.set(null);
    localStorage.removeItem(this.USER_KEY);
  }

  login(request: LoginRequest): Observable<UserResponse> {
    return this.http
      .post<ApiResponse<LoginResponse>>(`${this.baseUrl}/auth/token`, request, {
        withCredentials: true,
      })
      .pipe(
        map((response) => response.result),
        tap((result) => {
          const token = result.accessToken;
          if (token) {
            this.setToken(token);
          }
        }),
        switchMap(() => this.getUserProfile()),
        tap((user) => {
          this.setUser(user);
        })
      );
  }

  refreshToken(): Observable<string> {
    return this.http
      .post<ApiResponse<LoginResponse>>(
        `${this.baseUrl}/auth/refresh`,
        {},
        { withCredentials: true }
      )
      .pipe(
        map((response) => response.result),
        map((result) => {
          const token = result.accessToken;
          if (!token) {
            throw new Error('No access token returned from refresh API');
          }
          this.setToken(token);
          return token;
        })
      );
  }

  logout(): void {
    const token = this.getToken();
    this.http
      .post(
        `${this.baseUrl}/auth/logout`,
        { token },
        { withCredentials: true }
      )
      .subscribe({
        next: () => {},
        error: () => {},
      });

    this.removeToken();
    this.clearUser();
  }

  getUserProfile(): Observable<UserResponse> {
    return this.http
      .get<ApiResponse<UserResponse>>(`${this.baseUrl}/users/my-info`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.result));
  }
}
