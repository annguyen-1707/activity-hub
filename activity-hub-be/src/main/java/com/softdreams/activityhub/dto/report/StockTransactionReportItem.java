package com.softdreams.activityhub.dto.report;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransactionReportItem {
    Integer stt;
    String transactionId;
    String typeLabel;
    String referenceId;
    String productName;
    String quantity;
    String createdByName;
    String createdAt;
    String note;
}

