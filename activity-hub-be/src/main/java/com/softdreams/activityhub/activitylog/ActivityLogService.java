package com.softdreams.activityhub.activitylog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.softdreams.activityhub.service.JasperReportService;
import com.softdreams.activityhub.infrastructure.outbox.OutboxService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.report.ActivityLogReportItem;
import com.softdreams.activityhub.dto.response.ActivityLogResponse;
import com.softdreams.activityhub.dto.response.TargetTypeResponse;
import com.softdreams.activityhub.entity.ActivityLog;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.ActivityLogMapper;
import com.softdreams.activityhub.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityLogService {

    ActivityLogRepository activityLogRepository;
    UserRepository userRepository;
    ActivityLogMapper activityLogMapper;
    HttpServletRequest request;
    JasperReportService jasperReportService;
    OutboxService outboxService;

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

        User user = userRepository
                .findById(event.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
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

    public void log(EventType eventType, TargetType targetType, String targetId) {
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

        outboxService.save(event);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ActivityLogResponse> search(
            Pageable pageable, String keyword, EventType eventType, TargetType targetType, LocalDateTime fromDate, LocalDateTime toDate) {
        return activityLogRepository
                .search(keyword, eventType, targetType, fromDate, toDate, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ActivityLogResponse> getLogsForExport(
            String keyword, EventType eventType, TargetType targetType, LocalDateTime fromDate, LocalDateTime toDate) {
        return activityLogRepository
                .findForExport(keyword, eventType, targetType, fromDate, toDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public byte[] exportPdf(
            String keyword, EventType eventType, TargetType targetType, LocalDateTime fromDate, LocalDateTime toDate) {
        List<ActivityLogResponse> list = getLogsForExport(keyword, eventType, targetType, fromDate, toDate);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter displayDtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        AtomicInteger counter = new AtomicInteger(1);
        List<ActivityLogReportItem> reportItems = list.stream().map(log -> {
            String eventLabel = log.getEventTypeLabel() != null ? log.getEventTypeLabel() : (log.getEventType() != null ? log.getEventType().getLabel() : "");
            String targetLabel = log.getTargetTypeLabel() != null ? log.getTargetTypeLabel() : (log.getTargetType() != null ? log.getTargetType().getLabel() : "");

            return ActivityLogReportItem.builder()
                    .stt(counter.getAndIncrement())
                    .username(log.getUsername() != null ? log.getUsername() : "—")
                    .fullName(log.getFullName() != null && !log.getFullName().isBlank() ? log.getFullName() : "—")
                    .eventTypeLabel(eventLabel)
                    .targetTypeLabel(targetLabel)
                    .targetId(log.getTargetId() != null ? log.getTargetId() : "—")
                    .ipAddress(log.getIpAddress() != null ? log.getIpAddress() : "—")
                    .createdAt(log.getCreatedAt() != null ? log.getCreatedAt().format(dtf) : "")
                    .build();
        }).toList();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "NHẬT KÝ HOẠT ĐỘNG HỆ THỐNG");

        String timeFilter = "Khoảng thời gian: ";
        if (fromDate != null && toDate != null) {
            timeFilter += "Từ " + fromDate.format(displayDtf) + " đến " + toDate.format(displayDtf);
        } else if (fromDate != null) {
            timeFilter += "Từ " + fromDate.format(displayDtf);
        } else if (toDate != null) {
            timeFilter += "Đến " + toDate.format(displayDtf);
        } else {
            timeFilter += "Toàn bộ thời gian";
        }
        parameters.put("filterInfo", timeFilter);

        User currentUser = getMyUser();
        parameters.put("printedBy", currentUser != null ? currentUser.getUsername() : "Admin");
        parameters.put("printedAt", LocalDateTime.now().format(dtf));
        parameters.put("totalRecords", reportItems.size());

        return jasperReportService.exportToPdf("activity_logs_report", parameters, reportItems);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TargetTypeResponse> getTargetTypes() {
        return Arrays.stream(TargetType.values())
                .map(t -> TargetTypeResponse.builder()
                        .name(t.name())
                        .value(t.name())
                        .label(t.getLabel())
                        .build())
                .toList();
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
