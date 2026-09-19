package com.softdreams.activityhub.dto.projection;

import java.math.BigDecimal;

public interface OrderStatisticsProjection {
    Long getTotalOrders();

    BigDecimal getTotalRevenue();

    Long getPendingCount();

    Long getCompletedCount();

    Long getCancelledCount();

    Long getCreatedCount();

    Long getConfirmedCount();
}
