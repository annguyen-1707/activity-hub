package com.softdreams.activityhub.dto.projection;

import java.math.BigDecimal;

public interface DailyRevenueProjection {
    String getOrderDate();

    BigDecimal getDailyRevenue();

    Long getDailyOrders();
}

