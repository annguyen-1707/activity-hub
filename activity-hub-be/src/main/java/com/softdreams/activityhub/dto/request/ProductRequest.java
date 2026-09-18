package com.softdreams.activityhub.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.softdreams.activityhub.enums.CategoryEnum;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {

    @NotBlank
    String name;

    @NotNull
    @PositiveOrZero
    BigDecimal price;

    @NotNull
    CategoryEnum category;

    double rate;

    String description;

    String image;
}
