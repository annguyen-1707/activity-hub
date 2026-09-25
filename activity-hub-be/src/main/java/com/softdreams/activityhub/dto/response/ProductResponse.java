package com.softdreams.activityhub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.softdreams.activityhub.enums.CategoryEnum;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    String id;
    String name;
    BigDecimal price;
    String categoryName;
    String categoryId;
    String categoryCode;
    BigDecimal rate;
    Integer totalReviews;
    int quantity;
    String description;
    String image;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
