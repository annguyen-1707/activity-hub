import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

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

    const user = authService.getCurrentUser()();

    const hasRole = user?.roles?.some(
      role => allowedRoles.includes(role.name)
    );

    if (hasRole) {
      return true;
    }

    return router.createUrlTree(['/404']);
  };
};
