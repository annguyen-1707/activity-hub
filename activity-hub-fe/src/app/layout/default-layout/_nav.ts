import { INavData } from '@coreui/angular';

export interface AppNavItem extends INavData {
  roles?: string[];
}

export const navItems: AppNavItem[] = [
  {
    title: true,
    name: 'Bán Hàng & Đơn Hàng',
    roles: ["USER"]
  },
  {
    name: 'Cửa hàng',
    url: '/orders/create',
    iconComponent: { name: 'cil-basket' },
    badge: {
      color: 'success',
      text: 'POS'
    },
    roles: ["USER"]
  },
  {
    name: 'Giỏ hàng & Thanh toán',
    url: '/orders/checkout',
    iconComponent: { name: 'cil-cart' },
    roles: ["USER"]
  },
  {
    name: 'Lịch sử đơn hàng',
    url: '/orders/list',
    iconComponent: { name: 'cil-notes' },
    roles: ["USER"]
  },
  {
    title: true,
    name: 'Quản Trị Hệ Thống',
    roles: ["ADMIN"]
  },
  {
    name: 'Dashboard',
    url: '/dashboard',
    iconComponent: { name: 'cil-speedometer' },
    badge: {
      color: 'info',
      text: 'HOT'
    },
    roles: ["ADMIN"]
  },
  {
    name: 'Quản lý người dùng',
    url: '/users',
    iconComponent: { name: 'cil-user' },
    roles: ["ADMIN"]
  },
  {
    name: 'Quản lý vai trò',
    url: '/roles',
    iconComponent: { name: 'cil-lock-locked' },
    roles: ["ADMIN"]
  },
  {
    name: 'Nhật ký hoạt động',
    url: '/activity-logs',
    iconComponent: { name: 'cil-description' },
    roles: ["ADMIN"]
  },
  {
    name: 'Quản lý sản phẩm & Kho',
    url: '/products',
    iconComponent: { name: 'cil-tags' },
    roles: ["ADMIN"]
  },
  {
    name: 'Quản lý đơn hàng',
    url: '/admin-orders',
    iconComponent: { name: 'cil-cart' },
    roles: ["ADMIN"]
  }
];
