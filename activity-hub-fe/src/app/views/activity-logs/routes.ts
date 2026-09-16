import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/activity-logs.component').then(
        (m) => m.ActivityLogsComponent
      ),
    data: {
      title: 'Nhật ký hoạt động',
    },
  },
];

