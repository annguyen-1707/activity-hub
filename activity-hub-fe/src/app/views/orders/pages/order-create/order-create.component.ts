import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { CartItem, OrderRequest, PaymentMethod, Product } from '../../../../core/models/order.model';
import { OrderService } from '../../services/order.service';
import { ProductService } from '../../../../core/services/product.service';
import { CategoryService } from '../../../../core/services/category.service';

@Component({
  selector: 'app-order-create',
  standalone: true,
  templateUrl: './order-create.component.html',
  styleUrls: ['./order-create.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    RowComponent,
    ColComponent,
    CardComponent,
    CardBodyComponent,
    ButtonDirective,
    AlertComponent,
    FormControlDirective,
    InputGroupComponent,
    InputGroupTextDirective,
    PaginationComponent,
    PageItemComponent,
    PageLinkDirective,
    SpinnerComponent,
    IconDirective,
  ],
})
export class OrderCreateComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly router = inject(Router);

  // Data signals
  products = signal<Product[]>([]);
  loadingProducts = signal<boolean>(false);
  cart = this.orderService.cart;
  totalCartQuantity = this.orderService.totalCartQuantity;
  totalCartAmount = this.orderService.totalCartAmount;

  // Filters ('' categoryId means "Tất cả")
  categoryOptions = signal<{ id: string; name: string }[]>([]);
  selectedCategoryId = signal<string>('');
  searchKeyword = signal<string>('');

  // Pagination
  page = signal<number>(0);
  pageSize = signal<number>(12);
  totalElements = signal<number>(0);
  totalPages = signal<number>(1);

  // Checkout Form signals
  shippingAddress = signal<string>('Tòa nhà Softdreams, Cầu Giấy, Hà Nội');
  paymentMethod = signal<PaymentMethod>('BANK');
  note = signal<string>('');

  // State signals
  submitting = signal<boolean>(false);
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');
  successModalVisible = signal<boolean>(false);
  createdOrderId = signal<string>('');
  createdOrderAmount = signal<number>(0);
  createdOrderItemsCount = signal<number>(0);

  // "Tất cả" (All) plus every active category fetched from the server
  categoryPills = computed(() => [{ id: '', name: 'Tất cả' }, ...this.categoryOptions()]);

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  loadCategories(): void {
    this.categoryService.getActiveCategories().subscribe({
      next: (list) => this.categoryOptions.set(list.map((c) => ({ id: c.id, name: c.name }))),
      error: () => {},
    });
  }

  loadProducts(): void {
    this.loadingProducts.set(true);
    this.productService
      .getProducts(this.page(), this.pageSize(), this.searchKeyword(), this.selectedCategoryId())
      .subscribe({
        next: (res) => {
          this.loadingProducts.set(false);
          this.products.set(res?.content || []);
          this.totalElements.set(res?.totalElements || 0);
          this.totalPages.set(res?.totalPages || 1);
        },
        error: () => {
          this.loadingProducts.set(false);
          this.showAlert('Không thể tải danh sách sản phẩm. Vui lòng kiểm tra lại backend!', 'danger');
        },
      });
  }

  onKeywordChange(keyword: string): void {
    this.searchKeyword.set(keyword);
    this.page.set(0);
    this.loadProducts();
  }

  selectCategory(categoryId: string): void {
    this.selectedCategoryId.set(categoryId);
    this.page.set(0);
    this.loadProducts();
  }

  resetFilters(): void {
    this.searchKeyword.set('');
    this.selectedCategoryId.set('');
    this.page.set(0);
    this.loadProducts();
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
      this.loadProducts();
    }
  }

  getPageNumbers(): number[] {
    const total = this.totalPages();
    const current = this.page();
    const pages: number[] = [];
    const start = Math.max(0, current - 2);
    const end = Math.min(total - 1, current + 2);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  selectPaymentMethod(method: PaymentMethod): void {
    this.paymentMethod.set(method);
  }

  getProductCartQuantity(productId: string): number {
    const item = this.cart().find((i) => i.product.id === productId);
    return item ? item.quantity : 0;
  }

  addToCart(product: Product): void {
    const currentCart = [...this.cart()];
    const index = currentCart.findIndex((item) => item.product.id === product.id);

    if (index > -1) {
      const item = currentCart[index];
      if (item.quantity < product.stock) {
        item.quantity += 1;
        item.subtotal = item.quantity * item.product.price;
        this.orderService.saveCart(currentCart);
        this.showAlert(`Đã tăng số lượng ${product.name}!`, 'success');
      } else {
        this.showAlert(`Sản phẩm ${product.name} chỉ còn ${product.stock} trong kho!`, 'danger');
      }
    } else {
      currentCart.push({
        product,
        quantity: 1,
        subtotal: product.price,
      });
      this.orderService.saveCart(currentCart);
      this.showAlert(`Đã thêm "${product.name}" vào giỏ hàng!`, 'success');
    }
  }

  updateQuantityByProduct(product: Product, delta: number): void {
    const item = this.cart().find((i) => i.product.id === product.id);
    if (item) {
      this.updateQuantity(item, delta);
    } else if (delta > 0) {
      this.addToCart(product);
    }
  }

  updateQuantity(item: CartItem, delta: number): void {
    const currentCart = [...this.cart()];
    const index = currentCart.findIndex((i) => i.product.id === item.product.id);

    if (index > -1) {
      const newQty = item.quantity + delta;
      if (newQty <= 0) {
        currentCart.splice(index, 1);
        this.showAlert(`Đã xóa ${item.product.name} khỏi giỏ!`, 'success');
      } else if (newQty > item.product.stock) {
        this.showAlert(`Số lượng vượt quá tồn kho (${item.product.stock})!`, 'danger');
        return;
      } else {
        currentCart[index].quantity = newQty;
        currentCart[index].subtotal = newQty * item.product.price;
      }
      this.orderService.saveCart(currentCart);
    }
  }

  removeFromCart(item: CartItem): void {
    const updated = this.cart().filter((i) => i.product.id !== item.product.id);
    this.orderService.saveCart(updated);
    this.showAlert(`Đã xóa ${item.product.name} khỏi giỏ hàng!`, 'success');
  }

  clearCart(): void {
    this.orderService.clearCart();
  }

  submitOrder(): void {
    if (this.cart().length === 0) {
      this.showAlert('Vui lòng chọn ít nhất 1 sản phẩm vào giỏ hàng!', 'danger');
      return;
    }

    if (!this.shippingAddress().trim()) {
      this.showAlert('Vui lòng nhập địa chỉ nhận hàng!', 'danger');
      return;
    }

    const cartSnapshot = [...this.cart()];
    const totalAmount = this.totalCartAmount();
    const itemsCount = this.totalCartQuantity();

    const payload: OrderRequest = {
      paymentMethod: this.paymentMethod(),
      shippingAddress: this.shippingAddress().trim(),
      note: this.note().trim() || undefined,
      items: cartSnapshot.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
    };

    this.submitting.set(true);

    this.orderService.createOrder(payload).subscribe({
      next: (order) => {
        this.submitting.set(false);
        this.createdOrderId.set(order?.id || 'Mới');
        this.createdOrderAmount.set(order?.totalAmount || totalAmount);
        this.createdOrderItemsCount.set(itemsCount);

        this.clearCart();
        this.successModalVisible.set(true);
      },
      error: (err) => {
        this.submitting.set(false);
        const msg =
          err?.error?.message ||
          'Có lỗi xảy ra khi tạo đơn hàng. Vui lòng kiểm tra lại backend!';
        this.showAlert(msg, 'danger');
      },
    });
  }

  goToOrderList(): void {
    this.successModalVisible.set(false);
    this.router.navigate(['/orders/list']);
  }

  closeSuccessModal(): void {
    this.successModalVisible.set(false);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
  }

  showAlert(message: string, type: 'success' | 'danger'): void {
    this.alertMessage.set(message);
    this.alertType.set(type);
    setTimeout(() => {
      this.alertMessage.set('');
    }, 3500);
  }
}
