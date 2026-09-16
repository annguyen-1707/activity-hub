import { INavData } from '@coreui/angular';

export const navItems: INavData[] = [
  {
    name: 'Dashboard',
    url: '/dashboard',
    iconComponent: { name: 'cil-speedometer' },
    badge: {
      color: 'info',
      text: 'HOT'
    }
  },
  {
    title: true,
    name: 'Bán Hàng & Đơn Hàng'
  },
  {
    name: 'Bán hàng (Tạo đơn)',
    url: '/orders/create',
    iconComponent: { name: 'cil-basket' },
    badge: {
      color: 'success',
      text: 'POS'
    }
  },
  {
    name: 'Lịch sử đơn hàng',
    url: '/orders/list',
    iconComponent: { name: 'cil-notes' }
  },
  {
    title: true,
    name: 'Quản Trị Hệ Thống'
  },
  {
    name: 'Quản lý người dùng',
    url: '/users',
    iconComponent: { name: 'cil-user' }
  },
  {
    name: 'Quản lý vai trò',
    url: '/roles',
    iconComponent: { name: 'cil-lock-locked' }
  },
  {
    name: 'Nhật ký hoạt động',
    url: '/activity-logs',
    iconComponent: { name: 'cil-description' }
  }
];
