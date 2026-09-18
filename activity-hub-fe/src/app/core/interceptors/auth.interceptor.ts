import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

const PUBLIC_AUTH_ENDPOINTS = ['/auth/token', '/auth/introspect', '/auth/refresh', '/auth/logout'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isPublicAuthRequest = PUBLIC_AUTH_ENDPOINTS.some((path) => req.url.includes(path));
  const token = isPublicAuthRequest ? null : authService.getToken();

  // Đính kèm withCredentials: true để trình duyệt tự động gửi và nhận HttpOnly Cookie
  let authReq = req.clone({
    withCredentials: true,
  });

  if (token) {
    authReq = authReq.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(authReq).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        // Nếu lỗi 401 xảy ra ở endpoint login hoặc refresh thì không tự refresh lại
        if (req.url.includes('/auth/token') || req.url.includes('/auth/refresh')) {
          if (req.url.includes('/auth/refresh')) {
            isRefreshing = false;
            refreshTokenSubject.next(null);
            authService.logout();
            router.navigate(['/authentication/login']);
          }
          return throwError(() => error);
        }

        // Tự động Refresh Token khi Access Token hết hạn
        if (!isRefreshing) {
          isRefreshing = true;
          refreshTokenSubject.next(null);

          return authService.refreshToken().pipe(
            switchMap((newToken: string) => {
              isRefreshing = false;
              refreshTokenSubject.next(newToken);

              // Retry lại request ban đầu với Access Token mới
              return next(
                req.clone({
                  setHeaders: {
                    Authorization: `Bearer ${newToken}`,
                  },
                  withCredentials: true,
                })
              );
            }),
            catchError((refreshErr) => {
              isRefreshing = false;
              refreshTokenSubject.next(null);
              authService.logout();
              router.navigate(['/authentication/login']);
              return throwError(() => refreshErr);
            })
          );
        } else {
          // Nếu đang trong tiến trình refresh, xếp hàng chờ Access Token mới
          return refreshTokenSubject.pipe(
            filter((token): token is string => token !== null),
            take(1),
            switchMap((newToken: string) => {
              return next(
                req.clone({
                  setHeaders: {
                    Authorization: `Bearer ${newToken}`,
                  },
                  withCredentials: true,
                })
              );
            })
          );
        }
      }

      return throwError(() => error);
    })
  );
};
