import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/categories.component').then(m => m.CategoriesComponent),
    data: {
      title: 'Quản lý danh mục'
    }
  }
];
