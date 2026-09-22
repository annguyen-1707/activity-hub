package com.softdreams.activityhub.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderRevenueProjection {
    LocalDateTime getCreatedAt();

    BigDecimal getTotalAmount();
}

