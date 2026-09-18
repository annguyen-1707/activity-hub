package com.softdreams.activityhub.dto.request;

import java.util.List;

import com.softdreams.activityhub.enums.StockTransactionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransactionRequest {

    @NotNull
    StockTransactionType type;

    String note;

    @NotEmpty
    @Valid
    List<StockTransactionLineRequest> lines;
}
