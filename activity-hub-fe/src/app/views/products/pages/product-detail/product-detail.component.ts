import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AlertComponent,
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  ColComponent,
  FormControlDirective,
  FormLabelDirective,
  FormSelectDirective,
  ModalBodyComponent,
  ModalComponent,
  ModalFooterComponent,
  ModalHeaderComponent,
  ModalTitleDirective,
  RowComponent,
  SpinnerComponent,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { ProductService } from '../../../../core/services/product.service';
import { CategoryService } from '../../../../core/services/category.service';
import { ProductRequest, ProductResponse } from '../../../../core/models/product.model';
import { CategoryOption } from '../../../../core/models/category.model';
import { ProductReviewListComponent } from '../../../../shared/components/product-review-list/product-review-list.component';

@Component({
  selector: 'app-management-product-detail',
  standalone: true,
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss'],
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardBodyComponent,
    ButtonDirective,
    SpinnerComponent,
    AlertComponent,
    FormControlDirective,
    FormLabelDirective,
    FormSelectDirective,
    ModalComponent,
    ModalHeaderComponent,
    ModalTitleDirective,
    ModalBodyComponent,
    ModalFooterComponent,
    IconDirective,
    ProductReviewListComponent,
  ],
})
export class ProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly fb = inject(FormBuilder);

  readonly stars = [1, 2, 3, 4, 5];

  productId = signal<string>('');
  product = signal<ProductResponse | null>(null);
  loading = signal<boolean>(true);
  saving = signal<boolean>(false);
  deleting = signal<boolean>(false);
  errorMessage = signal<string>('');
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');
  copiedId = signal<string>('');

  // Modals
  editModalVisible = signal<boolean>(false);
  deleteModalVisible = signal<boolean>(false);

  categoryOptions = signal<CategoryOption[]>([]);
  editForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.productId.set(id);
      this.loadProduct(id);
    } else {
      this.errorMessage.set('Không tìm thấy mã sản phẩm.');
      this.loading.set(false);
    }
  }

  initForm(): void {
    this.editForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      price: [0, [Validators.required, Validators.min(0)]],
      categoryId: ['', [Validators.required]],
      image: [''],
      description: [''],
    });
  }

  loadCategories(): void {
    this.categoryService.getActiveCategories().subscribe({
      next: (opts) => this.categoryOptions.set(opts),
      error: () => {},
    });
  }

  loadProduct(id: string): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.productService.getProductById(id).subscribe({
      next: (res) => {
        this.product.set(res);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Không thể tải thông tin sản phẩm hoặc sản phẩm không tồn tại.');
        this.loading.set(false);
      },
    });
  }

  openEditModal(): void {
    const prod = this.product();
    if (!prod) return;
    this.editForm.patchValue({
      name: prod.name,
      price: prod.price,
      categoryId: prod.category?.id || '',
      image: prod.image || '',
      description: prod.description || '',
    });
    this.editModalVisible.set(true);
  }

  saveEdit(): void {
    const prod = this.product();
    if (!prod || this.editForm.invalid) return;

    this.saving.set(true);
    const formVal = this.editForm.value;
    const req: ProductRequest = {
      name: formVal.name,
      price: Number(formVal.price),
      categoryId: formVal.categoryId,
      image: formVal.image || '',
      description: formVal.description || '',
    };

    this.productService.updateProduct(prod.id, req).subscribe({
      next: (updated) => {
        this.saving.set(false);
        this.editModalVisible.set(false);
        this.product.set(updated);
        this.showAlert('Cập nhật sản phẩm thành công!', 'success');
      },
      error: (err) => {
        this.saving.set(false);
        this.showAlert(err?.error?.message || 'Có lỗi xảy ra khi cập nhật!', 'danger');
      },
    });
  }

  openDeleteModal(): void {
    this.deleteModalVisible.set(true);
  }

  confirmDelete(): void {
    const prod = this.product();
    if (!prod) return;

    this.deleting.set(true);
    this.productService.deleteProduct(prod.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.deleteModalVisible.set(false);
        this.router.navigate(['/products']);
      },
      error: (err) => {
        this.deleting.set(false);
        this.showAlert(err?.error?.message || 'Không thể xóa sản phẩm này!', 'danger');
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

  formatCurrency(value?: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    try {
      return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
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
}
