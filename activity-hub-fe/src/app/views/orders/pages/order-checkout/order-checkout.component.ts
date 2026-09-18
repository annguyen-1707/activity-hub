import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  AlertComponent,
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  FormControlDirective,
  InputGroupComponent,
  InputGroupTextDirective,
  ModalBodyComponent,
  ModalComponent,
  RowComponent,
  SpinnerComponent,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { CartItem, OrderRequest, PaymentMethod } from '../../../../core/models/order.model';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-order-checkout',
  standalone: true,
  templateUrl: './order-checkout.component.html',
  styleUrls: ['./order-checkout.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    SpinnerComponent,
    AlertComponent,
    FormControlDirective,
    InputGroupComponent,
    InputGroupTextDirective,
    ModalComponent,
    ModalBodyComponent,
    IconDirective,
  ],
})
export class OrderCheckoutComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Cart from global OrderService
  cart = this.orderService.cart;
  totalCartQuantity = this.orderService.totalCartQuantity;
  totalCartAmount = this.orderService.totalCartAmount;

  // Checkout Form fields
  receiverName = signal<string>('');
  receiverPhone = signal<string>('0987654321');
  shippingAddress = signal<string>('Tòa nhà Softdreams, Cầu Giấy, Hà Nội');
  paymentMethod = signal<PaymentMethod>('BANK');
  note = signal<string>('');

  // States
  submitting = signal<boolean>(false);
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');
  successModalVisible = signal<boolean>(false);
  createdOrderId = signal<string>('');
  createdOrderAmount = signal<number>(0);
  createdOrderItemsCount = signal<number>(0);

  ngOnInit(): void {
    const user = this.authService.getCurrentUser()();
    if (user) {
      const name = `${user.lastName || ''} ${user.firstName || ''}`.trim() || user.username;
      this.receiverName.set(name);
    }
  }

  selectPaymentMethod(method: PaymentMethod): void {
    this.paymentMethod.set(method);
  }

  updateQuantity(item: CartItem, delta: number): void {
    const currentCart = [...this.cart()];
    const index = currentCart.findIndex((i) => i.product.id === item.product.id);

    if (index > -1) {
      const newQty = item.quantity + delta;
      if (newQty <= 0) {
        currentCart.splice(index, 1);
        this.showAlert(`Đã xóa "${item.product.name}" khỏi giỏ hàng!`, 'success');
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
    this.showAlert(`Đã xóa "${item.product.name}" khỏi giỏ hàng!`, 'success');
  }

  clearCart(): void {
    this.orderService.clearCart();
    this.showAlert('Đã xóa toàn bộ giỏ hàng!', 'success');
  }

  submitOrder(): void {
    if (this.cart().length === 0) {
      this.showAlert('Giỏ hàng đang trống. Vui lòng chọn sản phẩm trước khi thanh toán!', 'danger');
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

        this.orderService.clearCart();
        this.successModalVisible.set(true);
      },
      error: (err) => {
        this.submitting.set(false);
        const msg =
          err?.error?.message ||
          'Có lỗi xảy ra khi tạo đơn hàng. Vui lòng kiểm tra lại kết nối backend!';
        this.showAlert(msg, 'danger');
      },
    });
  }

  goToOrderList(): void {
    this.successModalVisible.set(false);
    this.router.navigate(['/orders/list']);
  }

  goToProducts(): void {
    this.successModalVisible.set(false);
    this.router.navigate(['/orders/create']);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }

  showAlert(message: string, type: 'success' | 'danger'): void {
    this.alertMessage.set(message);
    this.alertType.set(type);
    setTimeout(() => {
      this.alertMessage.set('');
    }, 3500);
  }
}
