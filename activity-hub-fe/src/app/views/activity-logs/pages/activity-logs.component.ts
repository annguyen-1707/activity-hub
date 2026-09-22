import { Component, OnInit, inject, signal } from '@angular/core';
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
import { ActivityEventType, ActivityLog, ActivityTargetType, TargetTypeItem } from '../../../core/models/activity-log.model';
import { ActivityLogService } from '../services/activity-log.service';

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

  logs = signal<ActivityLog[]>([]);
  loading = signal<boolean>(false);
  targetTypes = signal<TargetTypeItem[]>([]);

  // Filters
  keyword = signal<string>('');
  selectedEventType = signal<ActivityEventType | 'ALL'>('ALL');
  selectedTargetType = signal<ActivityTargetType | 'ALL'>('ALL');

  // Server-driven pagination
  page = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  totalPages = signal<number>(1);

  // Modal
  selectedLog = signal<ActivityLog | null>(null);
  detailModalVisible = signal<boolean>(false);

  ngOnInit(): void {
    this.loadTargetTypes();
    this.loadLogs();
  }

  loadTargetTypes(): void {
    this.activityLogService.getTargetTypes().subscribe({
      next: (types) => this.targetTypes.set(types),
      error: () => {
        this.targetTypes.set([
          { name: 'USER', label: 'Người dùng' },
          { name: 'ORDER', label: 'Đơn hàng' },
          { name: 'ROLE', label: 'Vai trò' },
          { name: 'PRODUCT', label: 'Sản phẩm' },
          { name: 'CATEGORY', label: 'Danh mục' },
          { name: 'REVIEW', label: 'Đánh giá' },
        ]);
      },
    });
  }

  loadLogs(): void {
    this.loading.set(true);
    this.activityLogService
      .getLogs(this.page(), this.pageSize(), this.keyword(), this.selectedEventType(), this.selectedTargetType())
      .subscribe({
        next: (res) => {
          this.loading.set(false);
          this.logs.set(res.content);
          this.totalElements.set(res.totalElements);
          this.totalPages.set(res.totalPages || 1);
        },
        error: () => {
          this.loading.set(false);
          this.logs.set([]);
          this.totalElements.set(0);
          this.totalPages.set(1);
        },
      });
  }

  onKeywordChange(value: string): void {
    this.keyword.set(value);
  }

  onSearch(): void {
    this.page.set(0);
    this.loadLogs();
  }

  onFilterChange(): void {
    this.page.set(0);
    this.loadLogs();
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
      this.loadLogs();
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSize.set(Number(newSize));
    this.page.set(0);
    this.loadLogs();
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

  getEventBadgeColor(type: ActivityEventType): string {
    switch (type) {
      case 'LOGIN':
        return 'info';
      case 'LOGOUT':
        return 'secondary';
      case 'CREATED':
      case 'APPROVED':
      case 'DONE':
        return 'success';
      case 'UPDATED':
        return 'warning';
      case 'DELETED':
      case 'REJECTED':
      case 'CANCEL':
        return 'danger';
      default:
        return 'primary';
    }
  }

  getTargetBadgeColor(type: ActivityTargetType | string): string {
    switch (type) {
      case 'ORDER':
        return 'success';
      case 'USER':
        return 'primary';
      case 'ROLE':
        return 'warning';
      case 'PRODUCT':
        return 'info';
      case 'CATEGORY':
        return 'dark';
      case 'REVIEW':
        return 'secondary';
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
