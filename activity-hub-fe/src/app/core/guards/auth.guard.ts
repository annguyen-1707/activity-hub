import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { UserResponse } from '../models/user.model';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  if (token) {
    return true;
  }

  return router.createUrlTree(['/authentication/login']);
};

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const token = authService.getToken();

    if (!token) {
      return router.createUrlTree(['/authentication/login']);
    }

    const checkRole = (user: UserResponse | null): boolean | UrlTree => {
      const hasRole = user?.roles?.some((role) =>
        allowedRoles.some(
          (allowed) =>
            role.name?.toUpperCase() === allowed.toUpperCase() ||
            role.name?.toUpperCase() === `ROLE_${allowed.toUpperCase()}` ||
            `ROLE_${role.name?.toUpperCase()}` === allowed.toUpperCase()
        )
      );

      if (hasRole) {
        return true;
      }

      // Tránh vòng lặp vô hạn: chuyển hướng thẳng tới /error-pages/404
      return router.createUrlTree(['/error-pages/404']);
    };

    const user = authService.getCurrentUser()();
    if (user) {
      return checkRole(user);
    }

    // Nếu có token nhưng thông tin user chưa kịp tải vào bộ nhớ,
    // gọi API lấy profile bất đồng bộ thay vì từ chối và gây loop
    return authService.getUserProfile().pipe(
      map((fetchedUser) => {
        authService.setUser(fetchedUser);
        return checkRole(fetchedUser);
      }),
      catchError(() => {
        authService.logout();
        return of(router.createUrlTree(['/authentication/login']));
      })
    );
  };
};
