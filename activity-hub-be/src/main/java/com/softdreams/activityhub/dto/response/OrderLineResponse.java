package com.softdreams.activityhub.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderLineResponse {
    String id;

    private String productId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
