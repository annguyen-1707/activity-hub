package com.softdreams.activityhub.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryDistributionItem {
    String categoryName;
    Long productCount;
}

