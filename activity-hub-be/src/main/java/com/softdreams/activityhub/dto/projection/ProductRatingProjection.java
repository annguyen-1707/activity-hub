package com.softdreams.activityhub.dto.projection;

public interface ProductRatingProjection {
    String getProductId();

    Double getAverageRating();

    Long getTotalReviews();
}
