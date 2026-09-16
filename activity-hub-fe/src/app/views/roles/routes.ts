import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/roles.component').then((m) => m.RolesComponent),
    data: {
      title: 'Quản lý vai trò',
    },
  },
];

