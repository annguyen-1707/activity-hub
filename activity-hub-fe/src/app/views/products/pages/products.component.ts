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
import { ProductService } from '../../../core/services/product.service';
import { StockTransactionService } from '../../../core/services/stock-transaction.service';
import { CategoryService } from '../../../core/services/category.service';
import { CategoryResponse } from '../../../core/models/category.model';
import {
  ProductRequest,
  ProductResponse,
} from '../../../core/models/product.model';
import {
  StockTransactionRequest,
  StockTransactionResponse,
  StockTransactionType,
  STOCK_TRANSACTION_TYPE_COLORS,
  STOCK_TRANSACTION_TYPE_LABELS,
} from '../../../core/models/stock-transaction.model';

@Component({
  selector: 'app-products',
  standalone: true,
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss'],
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
export class ProductsComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly stockService = inject(StockTransactionService);
  private readonly categoryService = inject(CategoryService);
  private readonly fb = inject(FormBuilder);

  // Active Tab: 0 = Products, 1 = Stock Transactions
  activeTab = signal<number>(0);

  // Category dynamic list
  availableCategories = signal<CategoryResponse[]>([]);
  readonly stockTypeLabels = STOCK_TRANSACTION_TYPE_LABELS;
  readonly stockTypeColors = STOCK_TRANSACTION_TYPE_COLORS;

  // PRODUCT STATE
  products = signal<ProductResponse[]>([]);
  productLoading = signal<boolean>(false);
  productKeyword = signal<string>('');
  productCategory = signal<string>('');
  productPage = signal<number>(0);
  productPageSize = signal<number>(10);
  productTotalElements = signal<number>(0);
  productTotalPages = signal<number>(0);

  // STOCK STATE
  stockTransactions = signal<StockTransactionResponse[]>([]);
  stockLoading = signal<boolean>(false);
  stockType = signal<StockTransactionType | ''>('');
  stockPage = signal<number>(0);
  stockPageSize = signal<number>(10);
  stockTotalElements = signal<number>(0);
  stockTotalPages = signal<number>(0);

  // MODALS & FORMS
  createProductModalVisible = signal<boolean>(false);
  editProductModalVisible = signal<boolean>(false);
  deleteProductModalVisible = signal<boolean>(false);
  stockModalVisible = signal<boolean>(false);

  selectedProduct = signal<ProductResponse | null>(null);
  saving = signal<boolean>(false);
  deleting = signal<boolean>(false);
  copiedId = signal<string>('');

  // Alerts
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');

  productForm!: FormGroup;
  stockForm!: FormGroup;

  // List of all products for stock modal dropdown
  allProductsList = signal<ProductResponse[]>([]);

  ngOnInit(): void {
    this.initForms();
    this.loadCategories();
    this.loadProducts();
    this.loadStockTransactions();
    this.loadAllProductsForDropdown();
  }

  loadCategories(): void {
    this.categoryService.getActiveCategories().subscribe({
      next: (cats) => this.availableCategories.set(cats || []),
      error: () => {},
    });
  }

  initForms(): void {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      price: [0, [Validators.required, Validators.min(0)]],
      categoryId: ['', [Validators.required]],
      rate: [5.0, [Validators.min(0), Validators.max(5)]],
      image: [''],
      description: [''],
    });

    this.stockForm = this.fb.group({
      type: ['IMPORT', [Validators.required]],
      productId: ['', [Validators.required]],
      quantity: [1, [Validators.required]],
      note: [''],
    });
  }

  // ==================== PRODUCT TAB ====================

  loadProducts(): void {
    this.productLoading.set(true);
    this.productService
      .searchProducts(this.productPage(), this.productPageSize(), this.productKeyword(), this.productCategory())
      .subscribe({
        next: (res) => {
          this.productLoading.set(false);
          this.products.set(res.content || []);
          this.productTotalElements.set(res.totalElements || 0);
          this.productTotalPages.set(res.totalPages || 1);
        },
        error: () => {
          this.productLoading.set(false);
          this.showAlert('Không thể tải danh sách sản phẩm', 'danger');
        },
      });
  }

  onProductSearch(): void {
    this.productPage.set(0);
    this.loadProducts();
  }

  onProductCategoryChange(catId: string): void {
    this.productCategory.set(catId);
    this.productPage.set(0);
    this.loadProducts();
  }

  onProductPageSizeChange(size: number): void {
    this.productPageSize.set(Number(size));
    this.productPage.set(0);
    this.loadProducts();
  }

  goToProductPage(p: number): void {
    if (p >= 0 && p < this.productTotalPages()) {
      this.productPage.set(p);
      this.loadProducts();
    }
  }

  getProductPageNumbers(): number[] {
    const total = this.productTotalPages();
    const current = this.productPage();
    const pages: number[] = [];
    const start = Math.max(0, current - 2);
    const end = Math.min(total - 1, current + 2);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  openCreateProductModal(): void {
    const firstCatId = this.availableCategories()[0]?.id || '';
    this.productForm.reset({
      name: '',
      price: 0,
      categoryId: firstCatId,
      rate: 5.0,
      image: '',
      description: '',
    });
    this.createProductModalVisible.set(true);
  }

  openEditProductModal(product: ProductResponse): void {
    this.selectedProduct.set(product);
    this.productForm.patchValue({
      name: product.name,
      price: product.price,
      categoryId: product.categoryId || '',
      rate: product.rate,
      image: product.image || '',
      description: product.description || '',
    });
    this.editProductModalVisible.set(true);
  }

  openDeleteProductModal(product: ProductResponse): void {
    this.selectedProduct.set(product);
    this.deleteProductModalVisible.set(true);
  }

  saveNewProduct(): void {
    if (this.productForm.invalid) return;
    this.saving.set(true);
    const formVal = this.productForm.value;
    const req: ProductRequest = {
      name: formVal.name,
      price: Number(formVal.price),
      categoryId: formVal.categoryId,
      rate: Number(formVal.rate) || 0,
      image: formVal.image || '',
      description: formVal.description || '',
    };

    this.productService.createProduct(req).subscribe({
      next: () => {
        this.saving.set(false);
        this.createProductModalVisible.set(false);
        this.showAlert('Thêm sản phẩm thành công!', 'success');
        this.loadProducts();
        this.loadAllProductsForDropdown();
      },
      error: (err) => {
        this.saving.set(false);
        this.showAlert(err?.error?.message || 'Có lỗi xảy ra khi tạo sản phẩm!', 'danger');
      },
    });
  }

  saveEditProduct(): void {
    const prod = this.selectedProduct();
    if (!prod || this.productForm.invalid) return;
    this.saving.set(true);
    const formVal = this.productForm.value;
    const req: ProductRequest = {
      name: formVal.name,
      price: Number(formVal.price),
      categoryId: formVal.categoryId,
      rate: Number(formVal.rate) || 0,
      image: formVal.image || '',
      description: formVal.description || '',
    };

    this.productService.updateProduct(prod.id, req).subscribe({
      next: () => {
        this.saving.set(false);
        this.editProductModalVisible.set(false);
        this.showAlert('Cập nhật thông tin sản phẩm thành công!', 'success');
        this.loadProducts();
        this.loadAllProductsForDropdown();
      },
      error: (err) => {
        this.saving.set(false);
        this.showAlert(err?.error?.message || 'Có lỗi xảy ra khi cập nhật!', 'danger');
      },
    });
  }

  confirmDeleteProduct(): void {
    const prod = this.selectedProduct();
    if (!prod) return;
    this.deleting.set(true);

    this.productService.deleteProduct(prod.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.deleteProductModalVisible.set(false);
        this.showAlert('Xóa sản phẩm thành công!', 'success');
        this.loadProducts();
        this.loadAllProductsForDropdown();
      },
      error: (err) => {
        this.deleting.set(false);
        this.showAlert(err?.error?.message || 'Không thể xóa sản phẩm này!', 'danger');
      },
    });
  }

  // ==================== STOCK TAB ====================

  loadStockTransactions(): void {
    this.stockLoading.set(true);
    const t = this.stockType() ? (this.stockType() as StockTransactionType) : undefined;
    this.stockService.searchTransactions(this.stockPage(), this.stockPageSize(), t).subscribe({
      next: (res) => {
        this.stockLoading.set(false);
        this.stockTransactions.set(res.content || []);
        this.stockTotalElements.set(res.totalElements || 0);
        this.stockTotalPages.set(res.totalPages || 1);
      },
      error: () => {
        this.stockLoading.set(false);
        this.showAlert('Không thể tải lịch sử biến động kho!', 'danger');
      },
    });
  }

  loadAllProductsForDropdown(): void {
    this.productService.searchProducts(0, 200).subscribe({
      next: (res) => this.allProductsList.set(res.content || []),
      error: () => {},
    });
  }

  onStockTypeChange(type: StockTransactionType | ''): void {
    this.stockType.set(type);
    this.stockPage.set(0);
    this.loadStockTransactions();
  }

  onStockPageSizeChange(size: number): void {
    this.stockPageSize.set(Number(size));
    this.stockPage.set(0);
    this.loadStockTransactions();
  }

  goToStockPage(p: number): void {
    if (p >= 0 && p < this.stockTotalPages()) {
      this.stockPage.set(p);
      this.loadStockTransactions();
    }
  }

  getStockPageNumbers(): number[] {
    const total = this.stockTotalPages();
    const current = this.stockPage();
    const pages: number[] = [];
    const start = Math.max(0, current - 2);
    const end = Math.min(total - 1, current + 2);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  openStockModal(): void {
    const firstProd = this.allProductsList()[0]?.id || '';
    this.stockForm.reset({
      type: 'IMPORT',
      productId: firstProd,
      quantity: 10,
      note: '',
    });
    this.stockModalVisible.set(true);
  }

  saveStockTransaction(): void {
    if (this.stockForm.invalid) return;
    this.saving.set(true);
    const formVal = this.stockForm.value;
    const req: StockTransactionRequest = {
      type: formVal.type,
      note: formVal.note || '',
      lines: [
        {
          productId: formVal.productId,
          quantity: Number(formVal.quantity),
        },
      ],
    };

    this.stockService.createManualTransaction(req).subscribe({
      next: () => {
        this.saving.set(false);
        this.stockModalVisible.set(false);
        this.showAlert('Thực hiện giao dịch kho thành công!', 'success');
        this.loadStockTransactions();
        this.loadProducts(); // refresh stock numbers
      },
      error: (err) => {
        this.saving.set(false);
        this.showAlert(err?.error?.message || 'Có lỗi xảy ra khi thực hiện giao dịch kho!', 'danger');
      },
    });
  }

  // ==================== HELPERS ====================

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
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
}
