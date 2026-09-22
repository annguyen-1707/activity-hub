import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  ButtonDirective,
  ButtonGroupComponent,
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  SpinnerComponent,
  TableDirective,
} from '@coreui/angular';
import { ChartjsComponent } from '@coreui/angular-chartjs';
import { IconDirective } from '@coreui/icons-angular';
import { ChartData, ChartOptions } from 'chart.js';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardOverviewResponse } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  imports: [
    CommonModule,
    RouterLink,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    ButtonGroupComponent,
    SpinnerComponent,
    TableDirective,
    ChartjsComponent,
    IconDirective,
  ],
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  readonly loading = signal<boolean>(true);
  readonly error = signal<string>('');
  readonly selectedDays = signal<number>(7);
  readonly data = signal<DashboardOverviewResponse | null>(null);

  readonly revenueChartData = computed<ChartData>(() => {
    const trend = this.data()?.revenueTrend || [];
    const labels = trend.map((t) => {
      const parts = t.orderDate.split('-');
      return parts.length === 3 ? `${parts[2]}/${parts[1]}` : t.orderDate;
    });
    const revenues = trend.map((t) => t.dailyRevenue);
    const orders = trend.map((t) => t.dailyOrders);

    return {
      labels,
      datasets: [
        {
          type: 'line',
          label: 'Doanh thu (VNĐ)',
          backgroundColor: 'rgba(13, 110, 253, 0.15)',
          borderColor: '#0d6efd',
          pointBackgroundColor: '#0d6efd',
          pointBorderColor: '#fff',
          pointHoverBackgroundColor: '#fff',
          pointHoverBorderColor: '#0d6efd',
          data: revenues,
          fill: true,
          tension: 0.35,
          yAxisID: 'y',
        },
        {
          type: 'bar',
          label: 'Số đơn hàng',
          backgroundColor: 'rgba(255, 193, 7, 0.75)',
          borderColor: '#ffc107',
          data: orders,
          yAxisID: 'y1',
        },
      ],
    };
  });

  readonly revenueChartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
      tooltip: {
        mode: 'index',
        intersect: false,
      },
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
      },
      y: {
        type: 'linear',
        display: true,
        position: 'left',
        beginAtZero: true,
        ticks: {
          callback: (val) => {
            if (typeof val === 'number') {
              if (val >= 1_000_000) return (val / 1_000_000).toFixed(1) + 'M';
              if (val >= 1_000) return (val / 1_000).toFixed(0) + 'k';
            }
            return val;
          },
        },
      },
      y1: {
        type: 'linear',
        display: true,
        position: 'right',
        beginAtZero: true,
        grid: {
          drawOnChartArea: false,
        },
        ticks: {
          stepSize: 1,
        },
      },
    },
  };

  readonly orderStatusChartData = computed<ChartData>(() => {
    const d = this.data();
    return {
      labels: ['Mới tạo', 'Đã xác nhận', 'Hoàn thành', 'Đã hủy'],
      datasets: [
        {
          data: [
            d?.createdOrders || 0,
            d?.confirmedOrders || 0,
            d?.completedOrders || 0,
            d?.cancelledOrders || 0,
          ],
          backgroundColor: ['#0d6efd', '#0dcaf0', '#198754', '#dc3545'],
          hoverOffset: 4,
        },
      ],
    };
  });

  readonly doughnutOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
      },
    },
  };

  readonly categoryChartData = computed<ChartData>(() => {
    const cats = this.data()?.categoryDistribution || [];
    return {
      labels: cats.map((c) => c.categoryName),
      datasets: [
        {
          data: cats.map((c) => c.productCount),
          backgroundColor: [
            '#321fdb',
            '#3399ff',
            '#f9b115',
            '#2eb85c',
            '#e55353',
            '#6f42c1',
            '#d63384',
            '#20c997',
          ],
          hoverOffset: 4,
        },
      ],
    };
  });

  ngOnInit(): void {
    this.loadOverview();
  }

  loadOverview(days: number = this.selectedDays()): void {
    this.loading.set(true);
    this.error.set('');
    this.selectedDays.set(days);

    this.dashboardService.getOverview(days).subscribe({
      next: (res) => {
        this.data.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(
          err?.error?.message || 'Không thể tải dữ liệu thống kê bảng điều khiển.'
        );
        this.loading.set(false);
      },
    });
  }

  formatCurrency(value?: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(value || 0);
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    try {
      return new Intl.DateTimeFormat('vi-VN', {
        dateStyle: 'short',
        timeStyle: 'short',
      }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
  }
}
