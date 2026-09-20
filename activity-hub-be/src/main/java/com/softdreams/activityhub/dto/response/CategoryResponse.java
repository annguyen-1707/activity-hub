package com.softdreams.activityhub.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    String id;
    String code;
    String name;
    String description;
    boolean active;
    long productCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
