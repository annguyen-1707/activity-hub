import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
import { ActivityEventType, ActivityLog, ActivityTargetType } from '../../../core/models/activity-log.model';
import { ActivityLogService } from '../../../core/services/activity-log.service';

@Component({
  selector: 'app-activity-logs',
  standalone: true,
  templateUrl: './activity-logs.component.html',
  styleUrls: ['./activity-logs.component.scss'],
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
export class ActivityLogsComponent implements OnInit {
  private readonly activityLogService = inject(ActivityLogService);

  allLogs = signal<ActivityLog[]>([]);
  loading = signal<boolean>(false);

  // Filters
  keyword = signal<string>('');
  selectedEventType = signal<string>('ALL');
  selectedTargetType = signal<string>('ALL');

  // Pagination
  page = signal<number>(0);
  pageSize = signal<number>(10);

  // Modal
  selectedLog = signal<ActivityLog | null>(null);
  detailModalVisible = signal<boolean>(false);

  filteredLogs = computed(() => {
    const logs = this.allLogs();
    const kw = this.keyword().trim().toLowerCase();
    const evt = this.selectedEventType();
    const tgt = this.selectedTargetType();

    return logs.filter((log) => {
      const matchKw =
        !kw ||
        log.username.toLowerCase().includes(kw) ||
        (log.fullName && log.fullName.toLowerCase().includes(kw)) ||
        log.eventId.toLowerCase().includes(kw) ||
        log.description.toLowerCase().includes(kw) ||
        (log.targetId && log.targetId.toString().toLowerCase().includes(kw));

      const matchEvt = evt === 'ALL' || log.eventType === evt;
      const matchTgt = tgt === 'ALL' || log.targetType === tgt;

      return matchKw && matchEvt && matchTgt;
    });
  });

  paginatedLogs = computed(() => {
    const list = this.filteredLogs();
    const start = this.page() * this.pageSize();
    return list.slice(start, start + this.pageSize());
  });

  totalPages = computed(() => {
    return Math.ceil(this.filteredLogs().length / this.pageSize()) || 1;
  });

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading.set(true);
    this.activityLogService.getLogs().subscribe({
      next: (logs) => {
        this.loading.set(false);
        this.allLogs.set(logs);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  onFilterChange(): void {
    this.page.set(0);
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSize.set(Number(newSize));
    this.page.set(0);
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

  openDetailModal(log: ActivityLog): void {
    this.selectedLog.set(log);
    this.detailModalVisible.set(true);
  }

  closeDetailModal(): void {
    this.detailModalVisible.set(false);
    this.selectedLog.set(null);
  }

  resetDefaultLogs(): void {
    this.activityLogService.clearLogs();
    this.loadLogs();
  }

  getEventBadgeColor(type: ActivityEventType): string {
    switch (type) {
      case 'USER_LOGIN':
        return 'info';
      case 'USER_LOGOUT':
        return 'secondary';
      case 'ORDER_CREATED':
      case 'USER_CREATED':
      case 'ROLE_CREATED':
        return 'success';
      case 'ORDER_UPDATED':
      case 'USER_UPDATED':
        return 'warning';
      case 'ORDER_DELETED':
      case 'USER_DELETED':
      case 'ROLE_DELETED':
        return 'danger';
      default:
        return 'primary';
    }
  }

  getEventLabel(type: ActivityEventType): string {
    switch (type) {
      case 'USER_LOGIN':
        return 'Đăng nhập';
      case 'USER_LOGOUT':
        return 'Đăng xuất';
      case 'ORDER_CREATED':
        return 'Tạo đơn hàng';
      case 'ORDER_UPDATED':
        return 'Sửa đơn hàng';
      case 'ORDER_DELETED':
        return 'Xóa đơn hàng';
      case 'USER_CREATED':
        return 'Tạo người dùng';
      case 'USER_UPDATED':
        return 'Sửa người dùng';
      case 'USER_DELETED':
        return 'Xóa người dùng';
      case 'ROLE_CREATED':
        return 'Tạo vai trò';
      case 'ROLE_DELETED':
        return 'Xóa vai trò';
      default:
        return type;
    }
  }

  getTargetBadgeColor(type: ActivityTargetType): string {
    switch (type) {
      case 'ORDER':
        return 'success';
      case 'USER':
        return 'primary';
      case 'ROLE':
        return 'warning';
      case 'AUTH':
        return 'info';
      default:
        return 'secondary';
    }
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

