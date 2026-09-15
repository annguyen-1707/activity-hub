import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    data: {
      title: 'Authentication'
    },
    children: [
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
      },
      {
        path: 'login',
        loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent),
        data: {
          title: 'Login'
        }
      },
      {
        path: 'register',
        loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent),
        data: {
          title: 'Register'
        }
      },
      {
        path: 'check-email',
        loadComponent: () => import('./pages/check-email/check-email.component').then(m => m.CheckEmailComponent),
        data: {
          title: 'Check Email'
        }
      },
      {
        path: 'password',
        children: [
          {
            path: 'reset',
            loadComponent: () => import('./pages/reset-password/reset-password.component').then(m => m.ResetPasswordComponent),
            data: {
              title: 'Reset Password'
            }
          },
          {
            path: 'change',
            loadComponent: () => import('./pages/change-password/change-password.component').then(m => m.ChangePasswordComponent),
            data: {
              title: 'Change Password'
            }
          },
          {
            path: 'changed',
            loadComponent: () => import('./pages/password-changed/password-changed.component').then(m => m.PasswordChangedComponent),
            data: {
              title: 'Password Changed'
            }
          }
        ]
      }
    ]
  }
];
