import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'orders',
    pathMatch: 'full'
  },
  {
    path: '',
    loadComponent: () => import('./layout').then(m => m.DefaultLayoutComponent),
    data: {
      title: 'Home'
    },
    children: [
      {
        path: 'dashboard',
        loadChildren: () => import('./views/dashboard/routes').then((m) => m.routes),
        canActivate: [roleGuard(['ADMIN'])],
      },
      {
        path: 'orders',
        loadChildren: () => import('./views/orders/routes').then((m) => m.routes),
        canActivate: [authGuard],
      },
      {
        path: 'users',
        loadChildren: () => import('./views/users/routes').then((m) => m.routes),
        canActivate: [roleGuard(['ADMIN'])],
      },
      {
        path: 'roles',
        loadChildren: () => import('./views/roles/routes').then((m) => m.routes),
        canActivate: [roleGuard(['ADMIN'])],
      },
      {
        path: 'activity-logs',
        loadChildren: () => import('./views/activity-logs/routes').then((m) => m.routes),
        canActivate: [roleGuard(['ADMIN'])],
      }
    ]
  },
  {
    path: 'login',
    redirectTo: 'authentication/login',
    pathMatch: 'full'
  },
  {
    path: '404',
    redirectTo: 'error-pages/404',
    pathMatch: 'full'
  },
  {
    path: 'authentication',
    loadChildren: () => import('./views/authentication/routes').then((m) => m.routes)
  },
  {
    path: 'error-pages',
    loadChildren: () => import('./views/error-pages/routes').then((m) => m.routes)
  },
  { path: '**', redirectTo: 'error-pages/404' }
];
