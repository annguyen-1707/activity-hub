package com.softdreams.activityhub.dto.response;

import java.time.LocalDateTime;

import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityLogResponse {
    String id;
    String eventId;
    String userId;
    String username;
    String fullName;
    EventType eventType;
    String eventTypeLabel;
    TargetType targetType;
    String targetTypeLabel;
    String targetId;
    String ipAddress;
    LocalDateTime createdAt;
}
