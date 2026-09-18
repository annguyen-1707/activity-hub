import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import {
  AlertComponent,
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  ColComponent,
  ContainerComponent,
  FormControlDirective,
  FormDirective,
  FormLabelDirective,
  InputGroupComponent,
  InputGroupTextDirective,
  RowComponent,
  SpinnerComponent,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { UserService } from '../../../users/services/user.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  standalone: true,
  host: {
    class: 'bg-body-tertiary min-vh-100 d-flex flex-row align-items-center py-4',
  },
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    AlertComponent,
    ButtonDirective,
    CardBodyComponent,
    CardComponent,
    ColComponent,
    ContainerComponent,
    FormControlDirective,
    FormDirective,
    FormLabelDirective,
    IconDirective,
    InputGroupComponent,
    InputGroupTextDirective,
    RowComponent,
    SpinnerComponent,
  ],
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  loading = signal<boolean>(false);
  errorMessage = signal<string>('');
  successMessage = signal<string>('');

  showPassword = signal<boolean>(false);
  showConfirmPassword = signal<boolean>(false);

  registerForm = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(4)]],
      lastName: ['', [Validators.required]],
      firstName: ['', [Validators.required]],
      dob: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    {
      validators: [this.passwordMatchValidator],
    }
  );

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    if (password && confirmPassword && password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }

  toggleShowPassword(): void {
    this.showPassword.update((v) => !v);
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword.update((v) => !v);
  }

  handleRegister(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      if (this.registerForm.errors?.['passwordMismatch']) {
        this.errorMessage.set('Mật khẩu và xác nhận mật khẩu không trùng khớp!');
      } else {
        this.errorMessage.set('Vui lòng điền đầy đủ và chính xác các thông tin!');
      }
      return;
    }

    const val = this.registerForm.getRawValue();
    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const payload = {
      username: val.username.trim(),
      password: val.password,
      firstName: val.firstName.trim(),
      lastName: val.lastName.trim(),
      dob: val.dob,
    };

    this.userService.createUser(payload).subscribe({
      next: (created) => {
        this.loading.set(false);
        this.successMessage.set(
          `Đăng ký tài khoản "${created.username}" thành công! Đang chuyển hướng đến trang đăng nhập...`
        );

        setTimeout(() => {
          this.router.navigate(['/authentication/login']);
        }, 1500);
      },
      error: (err) => {
        this.loading.set(false);
        const code = err?.error?.code;
        const msg = err?.error?.message;

        if (code === 1002) {
          this.errorMessage.set(
            `Tên đăng nhập "${val.username}" đã tồn tại trên hệ thống. Vui lòng chọn tên khác!`
          );
        } else if (code === 1003) {
          this.errorMessage.set('Tên đăng nhập phải có ít nhất 4 ký tự!');
        } else if (code === 1004) {
          this.errorMessage.set('Mật khẩu phải có ít nhất 6 ký tự!');
        } else if (code === 1008) {
          this.errorMessage.set('Độ tuổi của bạn chưa đủ điều kiện (tối thiểu 10 tuổi)!');
        } else {
          this.errorMessage.set(
            msg || 'Đăng ký tài khoản không thành công. Vui lòng kiểm tra lại thông tin!'
          );
        }
      },
    });
  }
}
