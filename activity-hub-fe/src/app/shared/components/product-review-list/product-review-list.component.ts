import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  PageItemComponent,
  PageLinkDirective,
  PaginationComponent,
  SpinnerComponent,
} from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';
import { ReviewService } from '../../../core/services/review.service';
import { ReviewResponse } from '../../../core/models/review.model';

@Component({
  selector: 'app-product-review-list',
  standalone: true,
  templateUrl: './product-review-list.component.html',
  styleUrls: ['./product-review-list.component.scss'],
  imports: [
    CommonModule,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    PaginationComponent,
    PageItemComponent,
    PageLinkDirective,
    SpinnerComponent,
    IconDirective,
  ],
})
export class ProductReviewListComponent implements OnChanges {
  @Input({ required: true }) productId!: string;

  private readonly reviewService = inject(ReviewService);

  readonly stars = [1, 2, 3, 4, 5];

  reviews = signal<ReviewResponse[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string>('');
  page = signal<number>(0);
  pageSize = signal<number>(5);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productId'] && this.productId) {
      this.page.set(0);
      this.loadReviews();
    }
  }

  loadReviews(): void {
    if (!this.productId) return;
    this.loading.set(true);
    this.errorMessage.set('');
    this.reviewService.getReviewsForProduct(this.productId, this.page(), this.pageSize()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.reviews.set(res?.content || []);
        this.totalElements.set(res?.totalElements || 0);
        this.totalPages.set(res?.totalPages || 0);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Không thể tải danh sách đánh giá.');
      },
    });
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.page.set(p);
      this.loadReviews();
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

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    try {
      return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
  }
}
