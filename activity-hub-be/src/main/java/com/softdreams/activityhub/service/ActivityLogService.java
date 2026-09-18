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

        if (event.getUserId() == null) {
            // Never requeue-loop the consumer over an unresolvable actor;
            // drop the record instead of retrying it forever.
            log.warn("Skipped activity log {}: no resolvable actor", event.getEventId());
            return;
        }

        User user = userRepository.findById(event.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        ActivityLog activityLog = ActivityLog.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .ipAddress(event.getIpAddress())
                .createdAt(event.getCreatedAt())
                .user(user)
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

        User actor = resolveActor(targetType, targetId);

        ActivityLogEvent event = ActivityLogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(actor != null ? actor.getId() : null)
                .eventType(eventType)
                .targetType(targetType)
                .targetId(targetId)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();

        producer.send(event);
    }

    private User resolveActor(TargetType targetType, String targetId) {
        User actor = getMyUser();
        if (actor != null) {
            return actor;
        }

        // Login/logout run on public endpoints, so there is no SecurityContext
        // yet; the user is the same one identified by targetId (their username).
        if (targetType == TargetType.USER && targetId != null) {
            return userRepository.findByUsername(targetId).orElse(null);
        }

        return null;
    }

    private User getMyUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // Login/logout happen on public endpoints, so there is no authenticated
        // actor yet; fall back to an unknown actor instead of failing the request.
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}
