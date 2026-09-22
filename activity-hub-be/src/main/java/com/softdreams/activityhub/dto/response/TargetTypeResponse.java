package com.softdreams.activityhub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TargetTypeResponse {
    String name;
    String value;
    String label;
}

