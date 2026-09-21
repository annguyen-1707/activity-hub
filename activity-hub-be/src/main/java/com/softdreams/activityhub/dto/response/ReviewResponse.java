package com.softdreams.activityhub.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    String id;
    String orderLineId;
    String productId;
    String productName;
    String userId;
    String username;
    Double rating;
    String comment;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
