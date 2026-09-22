package com.softdreams.activityhub.dto.report;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderReportItem {
    Integer stt;
    String orderId;
    String customerName;
    String productSummary;
    String totalAmount;
    String paymentMethod;
    String statusLabel;
    String createdAt;
    String shippingAddress;
}

