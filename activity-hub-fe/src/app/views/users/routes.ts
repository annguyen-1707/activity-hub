import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/users.component').then((m) => m.UsersComponent),
    data: {
      title: 'Quản lý người dùng',
    },
  },
];
