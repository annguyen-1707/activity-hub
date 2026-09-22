package com.softdreams.activityhub.dto.response.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.softdreams.activityhub.dto.response.ActivityLogResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardOverviewResponse {
    Long totalOrders;
    BigDecimal totalRevenue;
    Long pendingOrders;
    Long completedOrders;
    Long cancelledOrders;
    Long createdOrders;
    Long confirmedOrders;

    Long totalProducts;
    Long lowStockProducts;
    Long outOfStockProducts;

    Long totalUsers;

    List<DailyRevenueItem> revenueTrend;
    List<CategoryDistributionItem> categoryDistribution;
    List<ActivityLogResponse> recentActivities;
    List<TopSellingProductItem> topSellingProducts;
    List<TopRatedProductItem> topRatedProducts;
}
