package com.softdreams.activityhub.dto.request;

import java.math.BigDecimal;

import com.softdreams.activityhub.enums.PaymentMethodEnum;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    BigDecimal totalAmount;
    String status;
    PaymentMethodEnum paymentMethod;
    String shippingAddress;
    String note;
}
