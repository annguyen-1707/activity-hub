package com.softdreams.activityhub.dto.projection;

import java.math.BigDecimal;

public interface TopSellingProductProjection {
    String getProductId();

    String getProductName();

    String getProductImage();

    BigDecimal getPrice();

    String getCategoryName();

    Long getTotalSold();

    BigDecimal getTotalRevenue();
}

