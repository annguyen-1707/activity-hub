package com.softdreams.activityhub.dto.projection;

import java.math.BigDecimal;

public interface TopRatedProductProjection {
    String getProductId();

    String getProductName();

    String getProductImage();

    BigDecimal getPrice();

    String getCategoryName();

    Double getAverageRating();

    Long getTotalReviews();
}

