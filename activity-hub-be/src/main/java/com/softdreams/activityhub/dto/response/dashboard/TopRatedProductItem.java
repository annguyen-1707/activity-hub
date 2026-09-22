package com.softdreams.activityhub.dto.response.dashboard;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopRatedProductItem {
    String productId;
    String productName;
    String productImage;
    BigDecimal price;
    String categoryName;
    Double averageRating;
    Long totalReviews;
}

