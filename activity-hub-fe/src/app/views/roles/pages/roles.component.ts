import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AlertComponent,
  BadgeComponent,
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
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
import { RoleResponse } from '../../../core/models/user.model';
import { RoleService } from '../services/role.service';


@Component({
  selector: 'app-roles',
  standalone: true,
  templateUrl: './roles.component.html',
  styleUrls: ['./roles.component.scss'],
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
    BadgeComponent,
    SpinnerComponent,
    AlertComponent,
    FormControlDirective,
    FormLabelDirective,
    FormSelectDirective,
    InputGroupComponent,
    InputGroupTextDirective,
    PaginationComponent,
    PageItemComponent,
    PageLinkDirective,
    ModalComponent,
    ModalHeaderComponent,
    ModalTitleDirective,
    ModalBodyComponent,
    ModalFooterComponent,
    IconDirective,
  ],
})
export class RolesComponent implements OnInit {
  private readonly roleService = inject(RoleService);
  private readonly fb = inject(FormBuilder);

  roles = signal<RoleResponse[]>([]);
  loading = signal<boolean>(false);
  saving = signal<boolean>(false);
  deleting = signal<boolean>(false);
  keyword = signal<string>('');

  // Modals
  createModalVisible = signal<boolean>(false);
  deleteModalVisible = signal<boolean>(false);
  selectedRole = signal<RoleResponse | null>(null);

  // Alerts
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');

  isDelete = signal<boolean>(false);

  roleForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadRoles();
  }

  initForm(): void {
    this.roleForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.pattern(/^[A-Z0-9_]+$/)]],
      description: ['', [Validators.required]],
    });
  }

  loadRoles(): void {
    this.loading.set(true);
    this.roleService.getRoles().subscribe({
      next: (data) => {
        this.loading.set(false);
        this.roles.set(data || []);
      },
      error: () => {
        this.loading.set(false);
        // Fallback default roles if backend is not ready
        this.roles.set([
          { name: 'ADMIN', description: 'Quản trị viên toàn quyền hệ thống' },
          { name: 'USER', description: 'Người dùng thông thường' },
          { name: 'MANAGER', description: 'Quản lý bán hàng và xử lý đơn hàng' },
        ]);
      },
    });
  }

  page = signal<number>(0);
  pageSize = signal<number>(5);

  filteredRoles(): RoleResponse[] {
    const kw = this.keyword().trim().toLowerCase();
    if (!kw) return this.roles();
    return this.roles().filter(
      (r) => r.name.toLowerCase().includes(kw) || (r.description && r.description.toLowerCase().includes(kw))
    );
  }

  paginatedRoles = computed(() => {
    const list = this.filteredRoles();
    const start = this.page() * this.pageSize();
    return list.slice(start, start + this.pageSize());
  });

  filteredRolesCount = computed(() => {
    return this.filteredRoles().length;
  });

  totalPages = computed(() => {
    return Math.ceil(this.filteredRolesCount() / this.pageSize()) || 1;
  });

  onPageSizeChange(newSize: number): void {
    this.pageSize.set(Number(newSize));
    this.page.set(0);
  }

  goToPage(pageNum: number): void {
    if (pageNum >= 0 && pageNum < this.totalPages()) {
      this.page.set(pageNum);
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

  onKeywordChange(value: string): void {
    this.keyword.set(value);
    this.page.set(0);
  }

  openCreateModal(): void {
    this.roleForm.reset({
      name: '',
      description: '',
    });
    this.createModalVisible.set(true);
  }

  closeCreateModal(): void {
    this.createModalVisible.set(false);
    this.roleForm.reset();
  }

  saveRole(): void {
    if (this.roleForm.invalid) {
      this.roleForm.markAllAsTouched();
      return;
    }

    const val = this.roleForm.getRawValue();
    const payload = {
      name: val.name.trim().toUpperCase(),
      description: val.description.trim(),
    };

    this.saving.set(true);
    this.roleService.createRole(payload).subscribe({
      next: (newRole) => {
        this.saving.set(false);
        this.closeCreateModal();
        this.showAlert(`Đã tạo vai trò "${payload.name}" thành công!`, 'success');

        this.loadRoles();
      },
      error: (err) => {
        this.saving.set(false);
        const msg = err?.error?.message || 'Có lỗi xảy ra khi tạo vai trò!';
        this.showAlert(msg, 'danger');
      },
    });
  }

  openDeleteModal(role: RoleResponse): void {
    this.selectedRole.set(role);
    this.deleteModalVisible.set(true);
  }

  closeDeleteModal(): void {
    this.deleteModalVisible.set(false);
    this.selectedRole.set(null);
  }

  confirmDelete(): void {
    const role = this.selectedRole();
    if (!role) return;

    this.deleting.set(true);
    this.roleService.deleteRole(role.name).subscribe({
      next: () => {
        this.deleting.set(false);
        this.closeDeleteModal();
        this.showAlert(`Đã xóa vai trò "${role.name}" thành công!`, 'success');

        this.loadRoles();
      },
      error: (err) => {
        this.deleting.set(false);
        const msg = err?.error?.message || 'Có lỗi xảy ra khi xóa vai trò!';
        this.showAlert(msg, 'danger');
      },
    });
  }

  getBadgeColor(roleName: string): string {
    switch (roleName.toUpperCase()) {
      case 'ADMIN':
        return 'danger';
      case 'USER':
        return 'primary';
      case 'MANAGER':
      case 'STAFF':
        return 'warning';
      default:
        return 'info';
    }
  }

  showAlert(message: string, type: 'success' | 'danger'): void {
    this.alertMessage.set(message);
    this.alertType.set(type);
    setTimeout(() => {
      this.alertMessage.set('');
    }, 3500);
  }
}

