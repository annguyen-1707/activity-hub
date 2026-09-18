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
  RowComponent,
  SpinnerComponent,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { CartItem, OrderRequest, PaymentMethod, Product } from '../../../../core/models/order.model';
import { OrderService } from '../../services/order.service';

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
    IconDirective,
  ],
})
export class OrderCreateComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  // Data signals
  products = signal<Product[]>([]);
  cart = this.orderService.cart;
  totalCartQuantity = this.orderService.totalCartQuantity;
  totalCartAmount = this.orderService.totalCartAmount;
  selectedCategory = signal<string>('Tất cả');
  searchKeyword = signal<string>('');

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

  // Computed properties
  categories = computed(() => {
    const all = this.products().map((p) => p.category);
    return ['Tất cả', ...Array.from(new Set(all))];
  });

  filteredProducts = computed(() => {
    const cat = this.selectedCategory();
    const kw = this.searchKeyword().trim().toLowerCase();

    return this.products().filter((p) => {
      const matchCat = cat === 'Tất cả' || p.category === cat;
      const matchKw =
        !kw ||
        p.name.toLowerCase().includes(kw) ||
        p.description.toLowerCase().includes(kw) ||
        p.category.toLowerCase().includes(kw);
      return matchCat && matchKw;
    });
  });

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.products.set(this.orderService.getMockProducts());
  }

  selectCategory(category: string): void {
    this.selectedCategory.set(category);
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
        productName: item.product.name,
        quantity: item.quantity,
        unitPrice: item.product.price,
        subtotal: item.subtotal,
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
