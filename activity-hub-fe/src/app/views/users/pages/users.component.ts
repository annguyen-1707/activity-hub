import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  AlertComponent,
  BadgeComponent,
  ButtonCloseDirective,
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  FormCheckComponent,
  FormCheckInputDirective,
  FormCheckLabelDirective,
  FormControlDirective,
  FormLabelDirective,
  FormSelectDirective,
  InputGroupComponent,
  InputGroupTextDirective,
  ModalBodyComponent,
  ModalComponent,
  ModalFooterComponent,
  ModalHeaderComponent,
  ModalTitleDirective,
  PageItemComponent,
  PageLinkDirective,
  PaginationComponent,
  RowComponent,
  SpinnerComponent,
  TableDirective,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { Role, User } from '../../../core/models/user.model';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { ActivityLogService } from '../../../core/services/activity-log.service';

@Component({
  selector: 'app-users',
  standalone: true,
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    TableDirective,
    ButtonDirective,
    ButtonCloseDirective,
    BadgeComponent,
    SpinnerComponent,
    AlertComponent,
    ModalHeaderComponent,
    ModalTitleDirective,
    ModalBodyComponent,
    ModalFooterComponent,
    FormControlDirective,
    FormLabelDirective,
    FormSelectDirective,
    FormCheckComponent,
    FormCheckInputDirective,
    FormCheckLabelDirective,
    InputGroupComponent,
    InputGroupTextDirective,
    PaginationComponent,
    PageItemComponent,
    PageLinkDirective,
    IconDirective,
    ModalComponent,
  ],
})
export class UsersComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly activityLogService = inject(ActivityLogService);
  private readonly fb = inject(FormBuilder);

  // State signals
  users = signal<User[]>([]);
  roles = signal<Role[]>([]);
  loading = signal<boolean>(false);
  saving = signal<boolean>(false);
  deleting = signal<boolean>(false);

  // Pagination state
  page = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);
  keyword = signal<string>('');

  // Modals state
  modalVisible = signal<boolean>(false);
  deleteModalVisible = signal<boolean>(false);
  isEditMode = signal<boolean>(false);
  selectedUser = signal<User | null>(null);

  // Alerts
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger' | ''>('');

  // Selected roles in form
  selectedRoleNames = signal<string[]>([]);

  userForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadUsers();
    this.loadRoles();
  }

  initForm(): void {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.minLength(6)]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      dob: [''],
    });
  }

  loadUsers(): void {
    this.loading.set(true);
    this.userService
      .getUsers(this.page(), this.pageSize(), this.keyword())
      .subscribe({
        next: (response) => {
          this.loading.set(false);
          if (response) {
            this.users.set(response.content || []);
            this.totalElements.set(response.totalElements || 0);
            this.totalPages.set(response.totalPages || 0);
          }
        },
        error: (err) => {
          this.loading.set(false);
          this.showAlert(
            'Không thể tải danh sách người dùng. Vui lòng kiểm tra lại kết nối backend!',
            'danger'
          );
        },
      });
  }

  loadRoles(): void {
    this.userService.getRoles().subscribe({
      next: (response) => {
        if (response) {
          this.roles.set(response);
        }
      },
      error: () => {
        this.roles.set([
          { name: 'ADMIN', description: 'Quản trị viên' },
          { name: 'USER', description: 'Người dùng thông thường' },
        ]);
      },
    });
  }

  onSearch(): void {
    this.page.set(0);
    this.loadUsers();
  }

  onKeywordChange(value: string): void {
    this.keyword.set(value);
    if (!value) {
      this.page.set(0);
      this.loadUsers();
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSize.set(Number(newSize));
    this.page.set(0);
    this.loadUsers();
  }

  goToPage(pageNum: number): void {
    if (pageNum >= 0 && pageNum < this.totalPages()) {
      this.page.set(pageNum);
      this.loadUsers();
    }
  }

  getPageNumbers(): number[] {
    const total = this.totalPages();
    const current = this.page();
    const pages: number[] = [];

    const start = Math.max(0, current - 2);
    const end = Math.min(total - 1, current + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  openCreateModal(): void {
    this.isEditMode.set(false);
    this.selectedUser.set(null);
    this.selectedRoleNames.set(['USER']);
    this.userForm.reset({
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      dob: '',
    });
    this.userForm.get('username')?.enable();
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.modalVisible.set(true);
  }

  openEditModal(user: User): void {
    this.isEditMode.set(true);
    this.selectedUser.set(user);
    const userRoles = user.roles ? user.roles.map((r) => r.name) : [];
    this.selectedRoleNames.set(userRoles);

    this.userForm.reset({
      username: user.username,
      password: '',
      firstName: user.firstName,
      lastName: user.lastName,
      dob: user.dob || '',
    });
    this.userForm.get('username')?.disable();
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.setValidators([Validators.minLength(6)]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.modalVisible.set(true);
  }

  closeModal(): void {
    this.modalVisible.set(false);
    this.userForm.reset();
  }

  handleModalVisibleChange(event: boolean): void {
    this.modalVisible.set(event);
  }

  toggleRole(roleName: string): void {
    const current = [...this.selectedRoleNames()];
    const index = current.indexOf(roleName);
    if (index > -1) {
      current.splice(index, 1);
    } else {
      current.push(roleName);
    }
    this.selectedRoleNames.set(current);
  }

  isRoleSelected(roleName: string): boolean {
    return this.selectedRoleNames().includes(roleName);
  }

  saveUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const formVal = this.userForm.getRawValue();
    this.saving.set(true);

    if (this.isEditMode()) {
      const userId = this.selectedUser()?.id;
      if (!userId) return;

      const updatePayload = {
        firstName: formVal.firstName,
        lastName: formVal.lastName,
        dob: formVal.dob || undefined,
        roles: this.selectedRoleNames(),
        ...(formVal.password ? { password: formVal.password } : {}),
      };

      this.userService.updateUser(userId, updatePayload).subscribe({
        next: (updated) => {
          this.saving.set(false);
          this.closeModal();
          this.showAlert('Cập nhật người dùng thành công!', 'success');

          const cur = this.authService.getCurrentUser()();
          this.activityLogService.recordLog({
            username: cur?.username || 'admin',
            fullName: cur ? `${cur.lastName} ${cur.firstName}` : undefined,
            eventType: 'USER_UPDATED',
            targetType: 'USER',
            targetId: userId,
            description: `Cập nhật thông tin người dùng: ${formVal.lastName} ${formVal.firstName}.`,
          });

          this.loadUsers();
          this.authService.setUser(updated);

        },
        error: (err) => {
          this.saving.set(false);
          const msg = err?.error?.message || 'Có lỗi xảy ra khi cập nhật người dùng!';
          this.showAlert(msg, 'danger');
        },
      });
    } else {
      const createPayload = {
        username: formVal.username,
        password: formVal.password,
        firstName: formVal.firstName,
        lastName: formVal.lastName,
        dob: formVal.dob || undefined,
      };

      this.userService.createUser(createPayload).subscribe({
        next: (created) => {
          this.saving.set(false);
          this.closeModal();
          this.showAlert('Tạo mới người dùng thành công!', 'success');

          const cur = this.authService.getCurrentUser()();
          this.activityLogService.recordLog({
            username: cur?.username || 'admin',
            fullName: cur ? `${cur.lastName} ${cur.firstName}` : undefined,
            eventType: 'USER_CREATED',
            targetType: 'USER',
            targetId: created?.id || createPayload.username,
            description: `Tạo mới tài khoản người dùng: @${createPayload.username} (${createPayload.lastName} ${createPayload.firstName}).`,
          });

          this.loadUsers();
        },
        error: (err) => {
          this.saving.set(false);
          let msg = 'Có lỗi xảy ra khi tạo người dùng!';
          if (err?.error?.message === 'USER_EXISTED' || err?.error?.code === 1002) {
            msg = 'Tên tài khoản (username) đã tồn tại trong hệ thống!';
          } else if (err?.error?.message) {
            msg = err.error.message;
          }
          this.showAlert(msg, 'danger');
        },
      });
    }
  }

  openDeleteModal(user: User): void {
    this.selectedUser.set(user);
    this.deleteModalVisible.set(true);
  }

  closeDeleteModal(): void {
    this.deleteModalVisible.set(false);
    this.selectedUser.set(null);
  }

  handleDeleteModalVisibleChange(event: boolean): void {
    this.deleteModalVisible.set(event);
  }

  confirmDelete(): void {
    const user = this.selectedUser();
    if (!user) return;

    this.deleting.set(true);
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.closeDeleteModal();
        this.showAlert(`Đã xóa người dùng "${user.username}" thành công!`, 'success');

        const cur = this.authService.getCurrentUser()();
        this.activityLogService.recordLog({
          username: cur?.username || 'admin',
          fullName: cur ? `${cur.lastName} ${cur.firstName}` : undefined,
          eventType: 'USER_DELETED',
          targetType: 'USER',
          targetId: user.id,
          description: `Xóa tài khoản người dùng: @${user.username}.`,
        });

        this.loadUsers();
      },
      error: (err) => {
        this.deleting.set(false);
        const msg = err?.error?.message || 'Có lỗi xảy ra khi xóa người dùng!';
        this.showAlert(msg, 'danger');
      },
    });
  }

  showAlert(message: string, type: 'success' | 'danger'): void {
    this.alertMessage.set(message);
    this.alertType.set(type);
    setTimeout(() => {
      this.alertMessage.set('');
      this.alertType.set('');
    }, 4500);
  }

  getRoleBadgeColor(roleName: string): string {
    switch (roleName.toUpperCase()) {
      case 'ADMIN':
        return 'danger';
      case 'USER':
        return 'primary';
      default:
        return 'info';
    }
  }

  getUserInitials(user: User): string {
    if (user.firstName && user.lastName) {
      return (user.firstName.charAt(0) + user.lastName.charAt(0)).toUpperCase();
    }
    return (user.username || 'U').substring(0, 2).toUpperCase();
  }
}
