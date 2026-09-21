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
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/product-detail/product-detail.component').then(
        (m) => m.ProductDetailComponent
      ),
    data: {
      title: 'Chi tiết sản phẩm',
    },
  },
];
