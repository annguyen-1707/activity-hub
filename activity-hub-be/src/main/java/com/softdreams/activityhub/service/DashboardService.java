package com.softdreams.activityhub.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.projection.OrderRevenueProjection;
import com.softdreams.activityhub.dto.projection.OrderStatisticsProjection;
import com.softdreams.activityhub.dto.response.ActivityLogResponse;
import com.softdreams.activityhub.dto.response.dashboard.CategoryDistributionItem;
import com.softdreams.activityhub.dto.response.dashboard.DailyRevenueItem;
import com.softdreams.activityhub.dto.response.dashboard.DashboardOverviewResponse;
import com.softdreams.activityhub.dto.response.dashboard.TopRatedProductItem;
import com.softdreams.activityhub.dto.response.dashboard.TopSellingProductItem;
import com.softdreams.activityhub.entity.ActivityLog;
import com.softdreams.activityhub.mapper.ActivityLogMapper;
import com.softdreams.activityhub.repository.ActivityLogRepository;
import com.softdreams.activityhub.repository.CategoryRepository;
import com.softdreams.activityhub.repository.OrderLineRepository;
import com.softdreams.activityhub.repository.OrderRepository;
import com.softdreams.activityhub.repository.ProductRepository;
import com.softdreams.activityhub.repository.ReviewRepository;
import com.softdreams.activityhub.repository.UserRepository;
import com.softdreams.activityhub.repository.custom.DashboardCustomRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {
    OrderRepository orderRepository;
    OrderLineRepository orderLineRepository;
    ProductRepository productRepository;
    ReviewRepository reviewRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    ActivityLogRepository activityLogRepository;
    ActivityLogMapper activityLogMapper;

    DashboardCustomRepository dashboardCustomRepository;

//    @PreAuthorize("hasRole('ADMIN')")
//    public DashboardOverviewResponse getOverview(int days) {
//        int rangeDays = (days <= 0 || days > 365) ? 7 : days;
//        LocalDateTime fromDate = LocalDateTime.now().minusDays(rangeDays).withHour(0).withMinute(0).withSecond(0).withNano(0);
//
//        // 1. Order Statistics
//        OrderStatisticsProjection stats = orderRepository.getAdminStatistics();
//        long totalOrders = stats != null && stats.getTotalOrders() != null ? stats.getTotalOrders() : 0L;
//        BigDecimal totalRevenue = stats != null && stats.getTotalRevenue() != null ? stats.getTotalRevenue() : BigDecimal.ZERO;
//        long pendingOrders = stats != null && stats.getPendingCount() != null ? stats.getPendingCount() : 0L;
//        long completedOrders = stats != null && stats.getCompletedCount() != null ? stats.getCompletedCount() : 0L;
//        long cancelledOrders = stats != null && stats.getCancelledCount() != null ? stats.getCancelledCount() : 0L;
//        long createdOrders = stats != null && stats.getCreatedCount() != null ? stats.getCreatedCount() : 0L;
//        long confirmedOrders = stats != null && stats.getConfirmedCount() != null ? stats.getConfirmedCount() : 0L;
//
//        // 2. Product Counts
//        long totalProducts = productRepository.count();
//        long lowStockProducts = productRepository.countByQuantityLessThanEqual(10);
//        long outOfStockProducts = productRepository.countByQuantityEquals(0);
//
//        // 3. User Count
//        long totalUsers = userRepository.count();
//
//        // 4. Daily Revenue Trend (0-filled for missing days)
//        List<OrderRevenueProjection> projections = orderRepository.findOrderRevenueSince(fromDate);
//        Map<LocalDate, List<OrderRevenueProjection>> groupedByDate = projections.stream()
//                .collect(Collectors.groupingBy(p -> p.getCreatedAt().toLocalDate()));
//
//        List<DailyRevenueItem> revenueTrend = new ArrayList<>();
//        LocalDate cur = fromDate.toLocalDate();
//        LocalDate today = LocalDate.now();
//        while (!cur.isAfter(today)) {
//            List<OrderRevenueProjection> list = groupedByDate.getOrDefault(cur, Collections.emptyList());
//            BigDecimal dayRevenue = list.stream()
//                    .map(OrderRevenueProjection::getTotalAmount)
//                    .filter(Objects::nonNull)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            long dayOrders = list.size();
//
//            revenueTrend.add(DailyRevenueItem.builder()
//                    .orderDate(cur.toString())
//                    .dailyRevenue(dayRevenue)
//                    .dailyOrders(dayOrders)
//                    .build());
//
//            cur = cur.plusDays(1);
//        }
//
//        // 5. Category Distribution
//        List<CategoryDistributionItem> categoryDistribution = categoryRepository.getCategoryDistribution().stream()
//                .map(proj -> CategoryDistributionItem.builder()
//                        .categoryName(proj.getCategoryName() != null ? proj.getCategoryName() : "Khác")
//                        .productCount(proj.getProductCount() != null ? proj.getProductCount() : 0L)
//                        .build())
//                .toList();
//
//        // 6. Top Selling Products (top 5)
//        List<TopSellingProductItem> topSellingProducts = orderLineRepository.getTopSellingProducts(PageRequest.of(0, 5)).stream()
//                .map(p -> TopSellingProductItem.builder()
//                        .productId(p.getProductId())
//                        .productName(p.getProductName())
//                        .productImage(p.getProductImage())
//                        .price(p.getPrice())
//                        .categoryName(p.getCategoryName() != null ? p.getCategoryName() : "—")
//                        .totalSold(p.getTotalSold() != null ? p.getTotalSold() : 0L)
//                        .totalRevenue(p.getTotalRevenue() != null ? p.getTotalRevenue() : BigDecimal.ZERO)
//                        .build())
//                .toList();
//
//        // 7. Top Rated Products (top 5)
//        List<TopRatedProductItem> topRatedProducts = reviewRepository.getTopRatedProducts(PageRequest.of(0, 5)).stream()
//                .map(p -> TopRatedProductItem.builder()
//                        .productId(p.getProductId())
//                        .productName(p.getProductName())
//                        .productImage(p.getProductImage())
//                        .price(p.getPrice())
//                        .categoryName(p.getCategoryName() != null ? p.getCategoryName() : "—")
//                        .averageRating(p.getAverageRating() != null ? Math.round(p.getAverageRating() * 10.0) / 10.0 : 0.0)
//                        .totalReviews(p.getTotalReviews() != null ? p.getTotalReviews() : 0L)
//                        .build())
//                .toList();
//
//        // 8. Recent Activities (top 5)
//        Page<ActivityLog> logPage = activityLogRepository.findAll(
//                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));
//        List<ActivityLogResponse> recentActivities = logPage.stream()
//                .map(activityLogMapper::toResponse)
//                .toList();
//
//        return DashboardOverviewResponse.builder()
//                .totalOrders(totalOrders)
//                .totalRevenue(totalRevenue)
//                .pendingOrders(pendingOrders)
//                .completedOrders(completedOrders)
//                .cancelledOrders(cancelledOrders)
//                .createdOrders(createdOrders)
//                .confirmedOrders(confirmedOrders)
//                .totalProducts(totalProducts)
//                .lowStockProducts(lowStockProducts)
//                .outOfStockProducts(outOfStockProducts)
//                .totalUsers(totalUsers)
//                .revenueTrend(revenueTrend)
//                .categoryDistribution(categoryDistribution)
//                .recentActivities(recentActivities)
//                .topSellingProducts(topSellingProducts)
//                .topRatedProducts(topRatedProducts)
//                .build();
//    }

    @PreAuthorize("hasRole('ADMIN')")
    public DashboardOverviewResponse getOverview(int days, LocalDateTime fromDate, LocalDateTime toDate) {
        return dashboardCustomRepository.getDashboardOverview(days, fromDate, toDate);
    }
}
