import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'create',
    pathMatch: 'full',
  },
  {
    path: 'create',
    loadComponent: () =>
      import('./pages/order-create/order-create.component').then(
        (m) => m.OrderCreateComponent
      ),
    data: {
      title: 'Tạo đơn hàng',
    },
  },
  {
    path: 'list',
    loadComponent: () =>
      import('./pages/order-list/order-list.component').then(
        (m) => m.OrderListComponent
      ),
    data: {
      title: 'Danh sách đơn hàng',
    },
  },
];

