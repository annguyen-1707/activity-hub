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


    public ActivityLogResponse(
            String id,
            String eventId,
            String userId,
            String username,
            String firstName,
            String lastName,
            EventType eventType,
            TargetType targetType,
            String targetId,
            String ipAddress,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.username = username;
        this.fullName = firstName + lastName;
        this.eventType = eventType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;

        // Gán label sau hoặc sử dụng getter để xử lý
        this.eventTypeLabel = eventType != null
                ? eventType.getLabel()
                : null;

        this.targetTypeLabel = targetType != null
                ? targetType.getLabel()
                : null;
    }
}
