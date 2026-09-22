import { ActivityLog } from './activity-log.model';

export interface DailyRevenueItem {
  orderDate: string;
  dailyRevenue: number;
  dailyOrders: number;
}

export interface CategoryDistributionItem {
  categoryName: string;
  productCount: number;
}

export interface TopSellingProductItem {
  productId: string;
  productName: string;
  productImage?: string;
  price: number;
  categoryName: string;
  totalSold: number;
  totalRevenue: number;
}

export interface TopRatedProductItem {
  productId: string;
  productName: string;
  productImage?: string;
  price: number;
  categoryName: string;
  averageRating: number;
  totalReviews: number;
}

export interface DashboardOverviewResponse {
  totalOrders: number;
  totalRevenue: number;
  pendingOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  createdOrders: number;
  confirmedOrders: number;

  totalProducts: number;
  lowStockProducts: number;
  outOfStockProducts: number;

  totalUsers: number;

  revenueTrend: DailyRevenueItem[];
  categoryDistribution: CategoryDistributionItem[];
  recentActivities: ActivityLog[];
  topSellingProducts?: TopSellingProductItem[];
  topRatedProducts?: TopRatedProductItem[];
}
