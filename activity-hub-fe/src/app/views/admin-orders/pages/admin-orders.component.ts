import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AlertComponent,
  BadgeComponent,
  ButtonDirective,
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  FormControlDirective,
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
import { OrderResponse, OrderStatus, PaymentMethod } from '../../../core/models/order.model';
import { OrderService } from '../../orders/services/order.service';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
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
export class AdminOrdersComponent implements OnInit {
  private readonly orderService = inject(OrderService);

  orders = signal<OrderResponse[]>([]);
  loading = signal<boolean>(false);
  updatingStatus = signal<boolean>(false);
  deleting = signal<boolean>(false);

  // Filters
  keyword = signal<string>('');
  statusFilter = signal<string>('ALL');
  paymentFilter = signal<string>('ALL');

  // Pagination
  page = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  // Modals
  selectedOrder = signal<OrderResponse | null>(null);
  detailModalVisible = signal<boolean>(false);
  deleteModalVisible = signal<boolean>(false);
  statusConfirmModalVisible = signal<boolean>(false);
  targetStatusToChange = signal<OrderStatus | null>(null);

  copiedId = signal<string>('');
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');

  // KPI Metrics computed from current orders
  totalOrdersCount = computed(() => this.totalElements());
  completedCount = computed(() => this.orders().filter((o) => o.status === 'COMPLETED').length);
  pendingCount = computed(
    () => this.orders().filter((o) => o.status === 'CREATED' || o.status === 'CONFIRMED').length
  );
  cancelledCount = computed(() => this.orders().filter((o) => o.status === 'CANCELLED').length);
  totalRevenue = computed(() =>
    this.orders()
      .filter((o) => o.status !== 'CANCELLED')
      .reduce((sum, o) => sum + (Number(o.totalAmount) || 0), 0)
  );

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);

    this.orderService
      .searchAdminOrders(
        this.page(),
        this.pageSize(),
        this.keyword(),
        this.statusFilter(),
        this.paymentFilter()
      )
      .subscribe({
        next: (res) => {
          this.loading.set(false);
          if (res?.content) {
            this.orders.set(res.content);
            this.totalElements.set(res.totalElements || 0);
            this.totalPages.set(res.totalPages || 1);
          } else {
            this.fallbackLoadAll();
          }
        },
        error: () => {
          this.fallbackLoadAll();
        },
      });
  }

  private fallbackLoadAll(): void {
    this.orderService.getAllOrders().subscribe({
      next: (list) => {
        this.loading.set(false);
        const kw = this.keyword().trim().toLowerCase();
        let filtered = list;
        if (kw) {
          filtered = filtered.filter(
            (o) =>
              (o.id && o.id.toLowerCase().includes(kw)) ||
              (o.shippingAddress && o.shippingAddress.toLowerCase().includes(kw)) ||
              (o.username && o.username.toLowerCase().includes(kw)) ||
              (o.customerName && o.customerName.toLowerCase().includes(kw)) ||
              (o.note && o.note.toLowerCase().includes(kw))
          );
        }
        if (this.statusFilter() !== 'ALL') {
          filtered = filtered.filter((o) => o.status === this.statusFilter());
        }
        if (this.paymentFilter() !== 'ALL') {
          filtered = filtered.filter((o) => o.paymentMethod === this.paymentFilter());
        }
        this.orders.set(filtered);
        this.totalElements.set(filtered.length);
        this.totalPages.set(Math.ceil(filtered.length / this.pageSize()) || 1);
      },
      error: () => {
        this.loading.set(false);
        this.orders.set([]);
        this.showAlert('Không thể tải danh sách đơn hàng!', 'danger');
      },
    });
  }

  onSearch(): void {
    this.page.set(0);
    this.loadOrders();
  }

  onKeywordChange(kw: string): void {
    this.keyword.set(kw);
    if (!kw) {
      this.page.set(0);
      this.loadOrders();
    }
  }

  onStatusChange(status: string): void {
    this.statusFilter.set(status);
    this.page.set(0);
    this.loadOrders();
  }

  onPaymentChange(payment: string): void {
    this.paymentFilter.set(payment);
    this.page.set(0);
    this.loadOrders();
  }

  onPageSizeChange(size: number): void {
    this.pageSize.set(Number(size));
    this.page.set(0);
    this.loadOrders();
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
      this.loadOrders();
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

  // Modals & Actions
  openDetailModal(order: OrderResponse): void {
    this.selectedOrder.set(order);
    this.detailModalVisible.set(true);
  }

  closeDetailModal(): void {
    this.detailModalVisible.set(false);
    this.selectedOrder.set(null);
  }

  openDeleteModal(order: OrderResponse): void {
    this.selectedOrder.set(order);
    this.deleteModalVisible.set(true);
  }

  confirmDelete(): void {
    const order = this.selectedOrder();
    if (!order) return;
    this.deleting.set(true);

    this.orderService.deleteOrder(order.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.deleteModalVisible.set(false);
        this.detailModalVisible.set(false);
        this.showAlert(`Đã xóa đơn hàng ${order.id.substring(0, 8)}...`, 'success');
        this.loadOrders();
      },
      error: (err) => {
        this.deleting.set(false);
        this.showAlert(err?.error?.message || 'Không thể xóa đơn hàng này!', 'danger');
      },
    });
  }

  requestStatusChange(order: OrderResponse, newStatus: OrderStatus): void {
    this.selectedOrder.set(order);
    this.targetStatusToChange.set(newStatus);
    this.statusConfirmModalVisible.set(true);
  }

  confirmStatusChange(): void {
    const order = this.selectedOrder();
    const newStatus = this.targetStatusToChange();
    if (!order || !newStatus) return;

    this.updatingStatus.set(true);
    this.orderService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (updated) => {
        this.updatingStatus.set(false);
        this.statusConfirmModalVisible.set(false);
        if (this.selectedOrder()?.id === updated.id) {
          this.selectedOrder.set(updated);
        }
        this.showAlert(`Đã chuyển trạng thái đơn sang "${this.getStatusLabel(newStatus)}"`, 'success');
        this.loadOrders();
      },
      error: (err) => {
        this.updatingStatus.set(false);
        this.showAlert(err?.error?.message || 'Không thể cập nhật trạng thái đơn!', 'danger');
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

  // Formatters
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

  getStatusBadgeColor(status: OrderStatus): string {
    switch (status) {
      case 'COMPLETED':
        return 'success';
      case 'CONFIRMED':
        return 'warning';
      case 'CREATED':
        return 'info';
      case 'CANCELLED':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  getStatusLabel(status: OrderStatus): string {
    switch (status) {
      case 'CREATED':
        return 'Mới tạo';
      case 'CONFIRMED':
        return 'Đang chuẩn bị';
      case 'COMPLETED':
        return 'Hoàn thành';
      case 'CANCELLED':
        return 'Đã hủy';
      default:
        return status || '—';
    }
  }

  getPaymentLabel(method: PaymentMethod): string {
    return method === 'BANK' ? 'Chuyển khoản' : 'Tiền mặt (COD)';
  }
}
