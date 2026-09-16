import {Component, inject, signal} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  AlertComponent,
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  ColComponent,
  ContainerComponent,
  FormCheckComponent,
  FormCheckInputDirective,
  FormCheckLabelDirective,
  FormControlDirective,
  FormDirective,
  FormLabelDirective,
  InputGroupComponent,
  InputGroupTextDirective,
  RowComponent,
  RowDirective,
  SpinnerComponent,
  TooltipDirective
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import {FormBuilder, FormGroup, NgForm, ReactiveFormsModule, Validators} from '@angular/forms';
import {LoginRequest} from '../../../../core/models/auth.model';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  host: {
    class: 'bg-body-tertiary min-vh-100 d-flex flex-row align-items-center'
  },
  imports: [
    AlertComponent,
    ButtonDirective,
    CardBodyComponent,
    CardComponent,
    ColComponent,
    ContainerComponent,
    FormCheckComponent,
    FormCheckInputDirective,
    FormCheckLabelDirective,
    FormControlDirective,
    FormDirective,
    FormLabelDirective,
    IconDirective,
    InputGroupComponent,
    InputGroupTextDirective,
    RouterLink,
    RowComponent,
    RowDirective,
    SpinnerComponent,
    TooltipDirective,
    ReactiveFormsModule
  ]
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  loading = signal<boolean>(false);
  errorMessage = signal<string>('');

  loginForm = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  protected handleLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.loading.set(false);
        let msg = 'Đăng nhập thất bại. Vui lòng kiểm tra lại tài khoản hoặc mật khẩu!';
        if (err?.error?.message) {
          msg = err.error.message;
        }
        this.errorMessage.set(msg);
      }
    });
  }
}
