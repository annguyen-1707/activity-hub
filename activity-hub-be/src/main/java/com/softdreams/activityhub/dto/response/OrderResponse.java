package com.softdreams.activityhub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.softdreams.activityhub.enums.PaymentMethodEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String id;
    String userId;
    BigDecimal totalAmount;
    String status;
    LocalDateTime createdAt;
    PaymentMethodEnum paymentMethod;
    String shippingAddress;
    String note;
}
