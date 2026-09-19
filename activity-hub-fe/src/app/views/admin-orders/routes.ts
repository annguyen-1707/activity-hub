import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/admin-orders.component').then((m) => m.AdminOrdersComponent),
    data: {
      title: 'Quản lý đơn hàng',
    },
  },
];
