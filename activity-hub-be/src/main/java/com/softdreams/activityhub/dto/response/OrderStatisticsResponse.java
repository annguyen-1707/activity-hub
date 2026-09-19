package com.softdreams.activityhub.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatisticsResponse {
    long totalOrders;
    BigDecimal totalRevenue;
    long pendingCount;
    long completedCount;
    long cancelledCount;
    long createdCount;
    long confirmedCount;
}
