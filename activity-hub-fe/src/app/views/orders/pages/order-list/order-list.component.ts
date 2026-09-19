import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
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
import { OrderService } from '../../services/order.service';

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

  orders = signal<OrderResponse[]>([]);
  loading = signal<boolean>(false);
  keyword = signal<string>('');
  statusFilter = signal<string>('ALL');
  paymentFilter = signal<string>('ALL');

  page = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  // Detail Modal
  selectedOrder = signal<OrderResponse | null>(null);
  detailModalVisible = signal<boolean>(false);
  copiedId = signal<string>('');

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
        this.paymentFilter()
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
  }

  closeDetailModal(): void {
    this.detailModalVisible.set(false);
    this.selectedOrder.set(null);
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
