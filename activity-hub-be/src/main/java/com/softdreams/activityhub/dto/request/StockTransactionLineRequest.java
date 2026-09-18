package com.softdreams.activityhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockTransactionLineRequest {

    @NotBlank
    String productId;

    /**
     * IMPORT requires a positive quantity; ADJUSTMENT accepts a signed value
     * (negative to write stock down, positive to write it up).
     */
    @NotNull
    Integer quantity;
}
