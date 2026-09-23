package com.softdreams.activityhub.repository.custom;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.dto.response.ActivityLogResponse;
import com.softdreams.activityhub.dto.response.dashboard.CategoryDistributionItem;
import com.softdreams.activityhub.dto.response.dashboard.DailyRevenueItem;
import com.softdreams.activityhub.dto.response.dashboard.DashboardOverviewResponse;
import com.softdreams.activityhub.dto.response.dashboard.TopRatedProductItem;
import com.softdreams.activityhub.dto.response.dashboard.TopSellingProductItem;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DashboardCustomRepository {

    JdbcTemplate jdbcTemplate;

    public DashboardOverviewResponse getDashboardOverview(int days) {
        return getDashboardOverview(days, null, null);
    }

    public DashboardOverviewResponse getDashboardOverview(int days, LocalDateTime fromDate, LocalDateTime toDate) {
        int rangeDays = (days <= 0 || days > 365) ? 7 : days;

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_GetDashboardOverview")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("rangeDays", Types.INTEGER),
                            new SqlParameter("fromDate", Types.TIMESTAMP),
                            new SqlParameter("toDate", Types.TIMESTAMP)
                    )
                    // 1. Result Set 1: Thẻ KPI Tổng quan
                    .returningResultSet("kpiStats", (rs, rowNum) -> {
                        KpiSummary kpi = new KpiSummary();
                        kpi.setTotalOrders(rs.getLong("totalOrders"));
                        kpi.setTotalRevenue(rs.getBigDecimal("totalRevenue") != null ? rs.getBigDecimal("totalRevenue") : BigDecimal.ZERO);
                        kpi.setPendingOrders(rs.getLong("pendingOrders"));
                        kpi.setCompletedOrders(rs.getLong("completedOrders"));
                        kpi.setCancelledOrders(rs.getLong("cancelledOrders"));
                        kpi.setCreatedOrders(rs.getLong("createdOrders"));
                        kpi.setConfirmedOrders(rs.getLong("confirmedOrders"));
                        kpi.setTotalProducts(rs.getLong("totalProducts"));
                        kpi.setLowStockProducts(rs.getLong("lowStockProducts"));
                        kpi.setOutOfStockProducts(rs.getLong("outOfStockProducts"));
                        kpi.setTotalUsers(rs.getLong("totalUsers"));
                        return kpi;
                    })
                    // 2. Result Set 2: Xu hướng doanh thu & số đơn theo ngày
                    .returningResultSet("revenueTrend", (rs, rowNum) -> DailyRevenueItem.builder()
                            .orderDate(rs.getString("orderDate"))
                            .dailyRevenue(rs.getBigDecimal("dailyRevenue") != null ? rs.getBigDecimal("dailyRevenue") : BigDecimal.ZERO)
                            .dailyOrders(rs.getLong("dailyOrders"))
                            .build())
                    // 3. Result Set 3: Top 5 sản phẩm bán chạy nhất
                    .returningResultSet("topSellingProducts", (rs, rowNum) -> TopSellingProductItem.builder()
                            .productId(rs.getString("productId"))
                            .productName(rs.getString("productName"))
                            .productImage(rs.getString("productImage"))
                            .price(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : BigDecimal.ZERO)
                            .categoryName(rs.getString("categoryName"))
                            .totalSold(rs.getLong("totalSold"))
                            .totalRevenue(rs.getBigDecimal("totalRevenue") != null ? rs.getBigDecimal("totalRevenue") : BigDecimal.ZERO)
                            .build())
                    // 4. Result Set 4: Cơ cấu sản phẩm theo danh mục
                    .returningResultSet("categoryDistribution", (rs, rowNum) -> CategoryDistributionItem.builder()
                            .categoryName(rs.getString("categoryName"))
                            .productCount(rs.getLong("productCount"))
                            .build())
                    // 5. Result Set 5: Top 5 sản phẩm được đánh giá tốt nhất
                    .returningResultSet("topRatedProducts", (rs, rowNum) -> TopRatedProductItem.builder()
                            .productId(rs.getString("productId"))
                            .productName(rs.getString("productName"))
                            .productImage(rs.getString("productImage"))
                            .price(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price") : BigDecimal.ZERO)
                            .categoryName(rs.getString("categoryName"))
                            .averageRating(rs.getDouble("averageRating"))
                            .totalReviews(rs.getLong("totalReviews"))
                            .build())
                    // 6. Result Set 6: Top 5 nhật ký hoạt động gần nhất
                    .returningResultSet("recentActivities", (rs, rowNum) -> {
                        EventType evType = null;
                        String evStr = rs.getString("eventType");
                        if (evStr != null) {
                            try {
                                evType = EventType.valueOf(evStr);
                            } catch (IllegalArgumentException ignored) {}
                        }

                        TargetType tgType = null;
                        String tgStr = rs.getString("targetType");
                        if (tgStr != null) {
                            try {
                                tgType = TargetType.valueOf(tgStr);
                            } catch (IllegalArgumentException ignored) {}
                        }

                        Timestamp ts = rs.getTimestamp("createdAt");
                        LocalDateTime actCreatedAt = ts != null ? ts.toLocalDateTime() : null;

                        return ActivityLogResponse.builder()
                                .id(rs.getString("id"))
                                .eventId(rs.getString("eventId"))
                                .userId(rs.getString("userId"))
                                .username(rs.getString("username"))
                                .fullName(rs.getString("fullName"))
                                .eventType(evType)
                                .eventTypeLabel(evType != null ? evType.getLabel() : null)
                                .targetType(tgType)
                                .targetTypeLabel(tgType != null ? tgType.getLabel() : null)
                                .targetId(rs.getString("targetId"))
                                .ipAddress(rs.getString("ipAddress"))
                                .createdAt(actCreatedAt)
                                .build();
                    });

            SqlParameterSource input = new MapSqlParameterSource()
                    .addValue("rangeDays", rangeDays)
                    .addValue("fromDate", fromDate != null ? Timestamp.valueOf(fromDate) : null)
                    .addValue("toDate", toDate != null ? Timestamp.valueOf(toDate) : null);

            Map<String, Object> output = jdbcCall.execute(input);

            // Trích xuất các kết quả từ 6 Result Sets
            @SuppressWarnings("unchecked")
            List<KpiSummary> kpiList = (List<KpiSummary>) output.get("kpiStats");
            KpiSummary kpi = (kpiList != null && !kpiList.isEmpty()) ? kpiList.get(0) : new KpiSummary();

            @SuppressWarnings("unchecked")
            List<DailyRevenueItem> revenueTrend = (List<DailyRevenueItem>) output.getOrDefault("revenueTrend", Collections.emptyList());

            @SuppressWarnings("unchecked")
            List<TopSellingProductItem> topSellingProducts = (List<TopSellingProductItem>) output.getOrDefault("topSellingProducts", Collections.emptyList());

            @SuppressWarnings("unchecked")
            List<CategoryDistributionItem> categoryDistribution = (List<CategoryDistributionItem>) output.getOrDefault("categoryDistribution", Collections.emptyList());

            @SuppressWarnings("unchecked")
            List<TopRatedProductItem> topRatedProducts = (List<TopRatedProductItem>) output.getOrDefault("topRatedProducts", Collections.emptyList());

            @SuppressWarnings("unchecked")
            List<ActivityLogResponse> recentActivities = (List<ActivityLogResponse>) output.getOrDefault("recentActivities", Collections.emptyList());

            log.info("Lấy dữ liệu Dashboard qua SP thành công (rangeDays={}, totalOrders={}, totalRevenue={})",
                    rangeDays, kpi.getTotalOrders(), kpi.getTotalRevenue());

            return DashboardOverviewResponse.builder()
                    .totalOrders(kpi.getTotalOrders() != null ? kpi.getTotalOrders() : 0L)
                    .totalRevenue(kpi.getTotalRevenue() != null ? kpi.getTotalRevenue() : BigDecimal.ZERO)
                    .pendingOrders(kpi.getPendingOrders() != null ? kpi.getPendingOrders() : 0L)
                    .completedOrders(kpi.getCompletedOrders() != null ? kpi.getCompletedOrders() : 0L)
                    .cancelledOrders(kpi.getCancelledOrders() != null ? kpi.getCancelledOrders() : 0L)
                    .createdOrders(kpi.getCreatedOrders() != null ? kpi.getCreatedOrders() : 0L)
                    .confirmedOrders(kpi.getConfirmedOrders() != null ? kpi.getConfirmedOrders() : 0L)
                    .totalProducts(kpi.getTotalProducts() != null ? kpi.getTotalProducts() : 0L)
                    .lowStockProducts(kpi.getLowStockProducts() != null ? kpi.getLowStockProducts() : 0L)
                    .outOfStockProducts(kpi.getOutOfStockProducts() != null ? kpi.getOutOfStockProducts() : 0L)
                    .totalUsers(kpi.getTotalUsers() != null ? kpi.getTotalUsers() : 0L)
                    .revenueTrend(revenueTrend != null ? revenueTrend : Collections.emptyList())
                    .categoryDistribution(categoryDistribution != null ? categoryDistribution : Collections.emptyList())
                    .topSellingProducts(topSellingProducts != null ? topSellingProducts : Collections.emptyList())
                    .topRatedProducts(topRatedProducts != null ? topRatedProducts : Collections.emptyList())
                    .recentActivities(recentActivities != null ? recentActivities : Collections.emptyList())
                    .build();

        } catch (DataAccessException e) {
            log.error("Lỗi khi thực thi Stored Procedure sp_GetDashboardOverview", e);
            throw e;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class KpiSummary {
        private Long totalOrders = 0L;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private Long pendingOrders = 0L;
        private Long completedOrders = 0L;
        private Long cancelledOrders = 0L;
        private Long createdOrders = 0L;
        private Long confirmedOrders = 0L;
        private Long totalProducts = 0L;
        private Long lowStockProducts = 0L;
        private Long outOfStockProducts = 0L;
        private Long totalUsers = 0L;
    }
}
