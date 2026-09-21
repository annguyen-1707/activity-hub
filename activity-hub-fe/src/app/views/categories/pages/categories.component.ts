import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AlertComponent,
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
import { CategoryRequest, CategoryResponse } from '../../../core/models/category.model';
import { CategoryService } from '../../../core/services/category.service';

@Component({
  selector: 'app-categories',
  standalone: true,
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.scss'],
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
    SpinnerComponent,
    AlertComponent,
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
    ModalComponent,
    ModalHeaderComponent,
    ModalTitleDirective,
    ModalBodyComponent,
    ModalFooterComponent,
    IconDirective,
  ],
})
export class CategoriesComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);
  private readonly fb = inject(FormBuilder);

  categories = signal<CategoryResponse[]>([]);
  loading = signal<boolean>(false);
  saving = signal<boolean>(false);
  deleting = signal<boolean>(false);
  keyword = signal<string>('');

  // Pagination
  page = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  // Modals
  createModalVisible = signal<boolean>(false);
  editModalVisible = signal<boolean>(false);
  deleteModalVisible = signal<boolean>(false);
  selectedCategory = signal<CategoryResponse | null>(null);

  // Alerts
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');
  copiedId = signal<string>('');

  categoryForm!: FormGroup;

  // KPI summary metrics
  totalCount = computed(() => this.totalElements());
  activeCount = computed(() => this.categories().filter((c) => c.active).length);
  totalProductsSum = computed(() =>
    this.categories().reduce((sum, c) => sum + (c.productCount || 0), 0)
  );

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();
  }

  initForm(): void {
    this.categoryForm = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), Validators.pattern(/^[A-Za-z0-9_-]+$/)]],
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      description: [''],
      active: [true],
    });
  }

  loadCategories(): void {
    this.loading.set(true);
    this.categoryService
      .searchCategories(this.page(), this.pageSize(), this.keyword())
      .subscribe({
        next: (res) => {
          this.loading.set(false);
          this.categories.set(res?.content || []);
          this.totalElements.set(res?.totalElements || 0);
          this.totalPages.set(res?.totalPages || 1);
        },
        error: () => {
          this.loading.set(false);
          this.showAlert('Không thể tải danh sách danh mục từ máy chủ!', 'danger');
        },
      });
  }

  onKeywordChange(kw: string): void {
    this.keyword.set(kw);
    this.page.set(0);
    this.loadCategories();
  }

  onPageSizeChange(newSize: number): void {
    this.pageSize.set(Number(newSize));
    this.page.set(0);
    this.loadCategories();
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
      this.loadCategories();
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

  closeFormModal(visible: boolean): void {
    if (!visible) {
      this.createModalVisible.set(false);
      this.editModalVisible.set(false);
    }
  }

  openCreateModal(): void {
    this.categoryForm.reset({
      code: '',
      name: '',
      description: '',
      active: true,
    });
    this.createModalVisible.set(true);
  }

  openEditModal(category: CategoryResponse): void {
    this.selectedCategory.set(category);
    this.categoryForm.patchValue({
      code: category.code,
      name: category.name,
      description: category.description || '',
      active: category.active,
    });
    this.editModalVisible.set(true);
  }

  openDeleteModal(category: CategoryResponse): void {
    this.selectedCategory.set(category);
    this.deleteModalVisible.set(true);
  }

  saveCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formVal = this.categoryForm.value;
    const req: CategoryRequest = {
      code: formVal.code.trim().toUpperCase(),
      name: formVal.name.trim(),
      description: formVal.description?.trim() || '',
      active: formVal.active,
    };

    if (this.editModalVisible() && this.selectedCategory()) {
      this.categoryService.updateCategory(this.selectedCategory()!.id, req).subscribe({
        next: () => {
          this.saving.set(false);
          this.editModalVisible.set(false);
          this.showAlert(`Đã cập nhật danh mục "${req.name}" thành công!`, 'success');
          this.loadCategories();
        },
        error: (err) => {
          this.saving.set(false);
          this.showAlert(err?.error?.message || 'Lỗi khi cập nhật danh mục!', 'danger');
        },
      });
    } else {
      this.categoryService.createCategory(req).subscribe({
        next: () => {
          this.saving.set(false);
          this.createModalVisible.set(false);
          this.showAlert(`Đã tạo mới danh mục "${req.name}" thành công!`, 'success');
          this.loadCategories();
        },
        error: (err) => {
          this.saving.set(false);
          this.showAlert(err?.error?.message || 'Lỗi khi tạo danh mục!', 'danger');
        },
      });
    }
  }

  confirmDelete(): void {
    const cat = this.selectedCategory();
    if (!cat) return;

    this.deleting.set(true);
    this.categoryService.deleteCategory(cat.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.deleteModalVisible.set(false);
        this.showAlert(`Đã xóa danh mục "${cat.name}" thành công!`, 'success');
        this.loadCategories();
      },
      error: (err) => {
        this.deleting.set(false);
        this.showAlert(
          err?.error?.message ||
            'Không thể xóa danh mục này do đang có sản phẩm liên kết!',
          'danger'
        );
      },
    });
  }

  copyId(id: string): void {
    if (!id) return;
    navigator.clipboard?.writeText(id).then(() => {
      this.copiedId.set(id);
      setTimeout(() => this.copiedId.set(''), 2000);
    });
  }

  showAlert(message: string, type: 'success' | 'danger'): void {
    this.alertMessage.set(message);
    this.alertType.set(type);
    setTimeout(() => {
      if (this.alertMessage() === message) {
        this.alertMessage.set('');
      }
    }, 4000);
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    try {
      const d = new Date(dateStr);
      return new Intl.DateTimeFormat('vi-VN', {
        dateStyle: 'short',
        timeStyle: 'short',
      }).format(d);
    } catch {
      return dateStr;
    }
  }
}
