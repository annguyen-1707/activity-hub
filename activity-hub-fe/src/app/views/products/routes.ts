import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/products.component').then((m) => m.ProductsComponent),
    data: {
      title: 'Quản lý sản phẩm & Kho',
    },
  },
];
