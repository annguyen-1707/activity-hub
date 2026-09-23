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
    double rate;
    long totalReviews;
    int quantity;
    String description;
    String image;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public ProductResponse(String id, String name, BigDecimal price, String categoryName, String categoryId, String categoryCode, int quantity, String description, String image, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.categoryCode = categoryCode;
        this.quantity = quantity;
        this.description = description;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
