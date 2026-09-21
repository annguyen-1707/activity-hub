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
      title: 'Sản phẩm',
    },
  },
  {
    path: 'checkout',
    loadComponent: () =>
      import('./pages/order-checkout/order-checkout.component').then(
        (m) => m.OrderCheckoutComponent
      ),
    data: {
      title: 'Giỏ hàng & Thanh toán',
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
  {
    path: 'products/:id',
    loadComponent: () =>
      import('./pages/product-detail/product-detail.component').then(
        (m) => m.ProductDetailComponent
      ),
    data: {
      title: 'Chi tiết sản phẩm',
    },
  },
];

