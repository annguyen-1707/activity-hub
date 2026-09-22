import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
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
import { OrderResponse, OrderStatus, PaymentMethod } from '../../../../core/models/order.model';
import { ReviewRequest, ReviewResponse } from '../../../../core/models/review.model';
import { OrderService } from '../../services/order.service';
import { ReviewService } from '../../../../core/services/review.service';

@Component({
  selector: 'app-order-list',
  standalone: true,
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
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
export class OrderListComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly reviewService = inject(ReviewService);

  orders = signal<OrderResponse[]>([]);
  loading = signal<boolean>(false);
  exporting = signal<boolean>(false);
  keyword = signal<string>('');
  statusFilter = signal<string>('ALL');
  paymentFilter = signal<string>('ALL');
  fromDate = signal<string>('');
  toDate = signal<string>('');

  page = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  // Detail Modal
  selectedOrder = signal<OrderResponse | null>(null);
  detailModalVisible = signal<boolean>(false);
  copiedId = signal<string>('');

  // Reviews for the order currently open in the detail modal, keyed by orderLineId
  reviewsByLineId = signal<Record<string, ReviewResponse>>({});

  // Review Modal
  reviewModalVisible = signal<boolean>(false);
  reviewingLineId = signal<string>('');
  reviewingProductName = signal<string>('');
  reviewRating = signal<number>(5);
  reviewComment = signal<string>('');
  savingReview = signal<boolean>(false);

  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');

  // Computed KPI Metrics
  totalOrdersCount = computed(() => this.totalElements());

  completedOrdersCount = computed(() => {
    return this.orders().filter((o) => o.status === 'COMPLETED').length;
  });

  pendingOrdersCount = computed(() => {
    return this.orders().filter((o) => o.status === 'CREATED' || o.status === 'CONFIRMED').length;
  });

  totalRevenueSum = computed(() => {
    return this.orders().reduce((sum, o) => sum + (o.totalAmount || 0), 0);
  });

  filteredOrders = computed(() => {
    return this.orders();
  });

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);

    this.orderService
      .getMyOrders(
        this.page(),
        this.pageSize(),
        this.keyword(),
        this.statusFilter(),
        this.paymentFilter(),
        this.fromDate(),
        this.toDate()
      )
      .subscribe({
        next: (res) => {
          this.loading.set(false);
          if (res?.content) {
            this.orders.set(res.content);
            this.totalElements.set(res.totalElements || res.content.length);
            this.totalPages.set(res.totalPages || 1);
          } else {
            this.fallbackLoadAllOrders();
          }
        },
        error: () => {
          this.fallbackLoadAllOrders();
        },
      });
  }

  onStatusChange(val: string): void {
    this.statusFilter.set(val);
    this.page.set(0);
    this.loadOrders();
  }

  onPaymentChange(val: string): void {
    this.paymentFilter.set(val);
    this.page.set(0);
    this.loadOrders();
  }

  onDateChange(): void {
    this.page.set(0);
    this.loadOrders();
  }

  resetFilters(): void {
    this.keyword.set('');
    this.statusFilter.set('ALL');
    this.paymentFilter.set('ALL');
    this.fromDate.set('');
    this.toDate.set('');
    this.page.set(0);
    this.loadOrders();
  }

  exportPdf(): void {
    this.exporting.set(true);
    this.orderService
      .exportMyOrdersPdf(
        this.keyword(),
        this.statusFilter(),
        this.paymentFilter(),
        this.fromDate(),
        this.toDate()
      )
      .subscribe({
        next: (blob) => {
          this.exporting.set(false);
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `don-hang-cua-toi-${new Date().getTime()}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.showAlert('Xuất báo cáo PDF thành công!', 'success');
        },
        error: () => {
          this.exporting.set(false);
          this.showAlert('Lỗi khi xuất file PDF đơn hàng!', 'danger');
        },
      });
  }

  private fallbackLoadAllOrders(): void {
    this.orderService.getAllOrders().subscribe({
      next: (list) => {
        this.loading.set(false);
        const kw = this.keyword().trim().toLowerCase();
        const filtered = list.filter((o) => {
          if (!kw) return true;
          return (
            (o.id && o.id.toLowerCase().includes(kw)) ||
            (o.shippingAddress && o.shippingAddress.toLowerCase().includes(kw)) ||
            (o.note && o.note.toLowerCase().includes(kw))
          );
        });
        this.orders.set(filtered);
        this.totalElements.set(filtered.length);
        this.totalPages.set(Math.ceil(filtered.length / this.pageSize()) || 1);
      },
      error: () => {
        this.loading.set(false);
        this.orders.set([]);
      },
    });
  }

  onSearch(): void {
    this.page.set(0);
    this.loadOrders();
  }

  onKeywordChange(value: string): void {
    this.keyword.set(value);
    if (!value) {
      this.page.set(0);
      this.loadOrders();
    }
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
      this.loadOrders();
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSize.set(Number(newSize));
    this.page.set(0);
    this.loadOrders();
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

  openDetailModal(order: OrderResponse): void {
    this.selectedOrder.set(order);
    this.detailModalVisible.set(true);
    this.reviewsByLineId.set({});

    if (order.status === 'COMPLETED') {
      this.reviewService.getReviewsForOrder(order.id).subscribe({
        next: (reviews) => {
          const map: Record<string, ReviewResponse> = {};
          for (const r of reviews) {
            map[r.orderLineId] = r;
          }
          this.reviewsByLineId.set(map);
        },
        error: () => {},
      });
    }
  }

  closeDetailModal(): void {
    this.detailModalVisible.set(false);
    this.selectedOrder.set(null);
  }

  /** Reviews are only allowed for a COMPLETED order, and only within 30 days of delivery. */
  canReviewOrder(order: OrderResponse | null): boolean {
    if (!order || order.status !== 'COMPLETED' || !order.completedAt) {
      return false;
    }
    const deliveredAt = new Date(order.completedAt).getTime();
    const daysSince = (Date.now() - deliveredAt) / (1000 * 60 * 60 * 24);
    return daysSince <= 30;
  }

  getReviewForLine(orderLineId: string): ReviewResponse | undefined {
    return this.reviewsByLineId()[orderLineId];
  }

  openReviewModal(orderLineId: string, productName: string): void {
    const existing = this.getReviewForLine(orderLineId);
    this.reviewingLineId.set(orderLineId);
    this.reviewingProductName.set(productName);
    this.reviewRating.set(existing?.rating || 5);
    this.reviewComment.set(existing?.comment || '');
    this.reviewModalVisible.set(true);
  }

  closeReviewModal(): void {
    this.reviewModalVisible.set(false);
    this.reviewingLineId.set('');
  }

  saveReview(): void {
    const orderLineId = this.reviewingLineId();
    if (!orderLineId) return;

    const request: ReviewRequest = {
      rating: this.reviewRating(),
      comment: this.reviewComment().trim() || undefined,
    };

    const existing = this.getReviewForLine(orderLineId);
    const save$ = existing
      ? this.reviewService.updateReview(orderLineId, request)
      : this.reviewService.createReview(orderLineId, request);

    this.savingReview.set(true);
    save$.subscribe({
      next: (review) => {
        this.savingReview.set(false);
        this.reviewsByLineId.set({ ...this.reviewsByLineId(), [orderLineId]: review });
        this.closeReviewModal();
        this.showAlert(existing ? 'Đã cập nhật đánh giá!' : 'Cảm ơn bạn đã đánh giá sản phẩm!', 'success');
      },
      error: (err) => {
        this.savingReview.set(false);
        this.showAlert(err?.error?.message || 'Có lỗi xảy ra khi lưu đánh giá!', 'danger');
      },
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

  copyOrderId(id: string): void {
    if (!id) return;
    navigator.clipboard?.writeText(id).then(() => {
      this.copiedId.set(id);
      setTimeout(() => this.copiedId.set(''), 2000);
    });
  }

  formatDate(dateStr: string): string {
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

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
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
    return method === 'BANK' ? 'Chuyển khoản' : 'Tiền mặt';
  }
}
