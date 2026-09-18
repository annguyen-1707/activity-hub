package com.softdreams.activityhub.service;

import com.softdreams.activityhub.dto.ActivityLogEvent;
import com.softdreams.activityhub.entity.ActivityLog;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.producer.ActivityLogProducer;
import com.softdreams.activityhub.repository.ActivityLogRepository;
import com.softdreams.activityhub.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityLogService {

    ActivityLogRepository activityLogRepository;
    UserRepository userRepository;
    HttpServletRequest request;

    public void save(ActivityLogEvent event) {

        if (activityLogRepository.existsByEventId(event.getEventId())) {
            return;
        }
        ActivityLog activityLog = ActivityLog.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .ipAddress(event.getIpAddress())
                .createdAt(event.getCreatedAt())
                .build();

        activityLogRepository.save(activityLog);
    }

    private final ActivityLogProducer producer;

    public void log(
            EventType eventType,
            TargetType targetType,
            String targetId
    ) {
        String ipAddress = request.getRemoteAddr();

        ActivityLogEvent event = ActivityLogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(getMyUser().getId())
                .eventType(eventType)
                .targetType(targetType)
                .targetId(targetId)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();

        producer.send(event);
    }

    private User getMyUser() {
        User user;
        String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return user;
    }
}
