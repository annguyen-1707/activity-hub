package com.softdreams.activityhub.dto;

import java.time.LocalDateTime;

import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogEvent {

    String eventId;

    String userId;

    EventType eventType;

    TargetType targetType;

    String targetId;

    String ipAddress;

    LocalDateTime createdAt;
}