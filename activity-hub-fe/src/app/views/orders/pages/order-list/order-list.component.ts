import { Component, OnInit, inject, signal } from '@angular/core';
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
import { OrderResponse, OrderStatus } from '../../../../core/models/order.model';
import { OrderService } from '../../../../core/services/order.service';

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
  page = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  // Detail Modal
  selectedOrder = signal<OrderResponse | null>(null);
  detailModalVisible = signal<boolean>(false);

  // Alert
  alertMessage = signal<string>('');
  alertType = signal<'success' | 'danger'>('success');

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);

    // Call getMyOrders first; if user is admin or on failure fallback to getAllOrders
    this.orderService.getMyOrders(this.page(), this.pageSize(), this.keyword()).subscribe({
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
        // Fallback to getAllOrders if /orders/me fails
        this.fallbackLoadAllOrders();
      },
    });
  }

  private fallbackLoadAllOrders(): void {
    this.orderService.getAllOrders().subscribe({
      next: (list) => {
        this.loading.set(false);
        const filtered = list.filter((o) => {
          if (!this.keyword()) return true;
          const kw = this.keyword().toLowerCase();
          return (
            (o.id && o.id.toLowerCase().includes(kw)) ||
            (o.shippingAddress && o.shippingAddress.toLowerCase().includes(kw))
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

  openDetailModal(order: OrderResponse): void {
    this.selectedOrder.set(order);
    this.detailModalVisible.set(true);
  }

  closeDetailModal(): void {
    this.detailModalVisible.set(false);
    this.selectedOrder.set(null);
  }

  getStatusBadgeColor(status: OrderStatus | string): string {
    switch (status) {
      case 'CREATED':
        return 'primary';
      case 'CONFIRMED':
        return 'info';
      case 'COMPLETED':
        return 'success';
      case 'CANCELLED':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  getStatusLabel(status: OrderStatus | string): string {
    switch (status) {
      case 'CREATED':
        return 'Đã tạo';
      case 'CONFIRMED':
        return 'Đã xác nhận';
      case 'COMPLETED':
        return 'Hoàn thành';
      case 'CANCELLED':
        return 'Đã hủy';
      default:
        return status;
    }
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleString('vi-VN');
    } catch {
      return dateStr;
    }
  }
}

