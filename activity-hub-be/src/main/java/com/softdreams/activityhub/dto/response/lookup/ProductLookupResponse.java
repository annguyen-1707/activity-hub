package com.softdreams.activityhub.dto.response.lookup;

import com.softdreams.activityhub.dto.response.CategoryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductLookupResponse {
    String id;
    String name;
    int quantity;
}
