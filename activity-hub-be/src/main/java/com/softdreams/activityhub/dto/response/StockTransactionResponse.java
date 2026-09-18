package com.softdreams.activityhub.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.softdreams.activityhub.enums.StockTransactionType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransactionResponse {
    String id;
    StockTransactionType type;
    String typeLabel;
    String referenceId;
    String note;
    String createdByUsername;
    LocalDateTime createdAt;
    List<StockTransactionLineResponse> lines;
}
