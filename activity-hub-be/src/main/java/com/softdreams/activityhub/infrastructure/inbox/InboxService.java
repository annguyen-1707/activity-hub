package com.softdreams.activityhub.infrastructure.inbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softdreams.activityhub.activitylog.ActivityLogEvent;
import com.softdreams.activityhub.activitylog.ActivityLogService;
import com.softdreams.activityhub.enums.InboxStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxService {

    private final InboxEventRepository inboxEventRepository;
    private final ActivityLogService activityLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(ActivityLogEvent event) {

        String eventId = event.getEventId();

        // Kiểm tra event đã tồn tại
        if (inboxEventRepository.existsByEventId(eventId)) {
            log.info("Event already exists in Inbox: {}", eventId);
            return;
        }

        InboxEvent inboxEvent = new InboxEvent();
        inboxEvent.setEventId(eventId);
        inboxEvent.setEventType(event.getEventType().name());
        inboxEvent.setPayload(toJson(event));
        inboxEvent.setStatus(InboxStatus.RECEIVED);
        inboxEvent.setReceivedAt(LocalDateTime.now());

        inboxEventRepository.save(inboxEvent);

        // Cập nhật trạng thái đang xử lý
        inboxEvent.setStatus(InboxStatus.PROCESSING);

        // Xử lý nghiệp vụ ActivityLog
        activityLogService.save(event);

        // Xử lý thành công
        inboxEvent.setStatus(InboxStatus.PROCESSED);
        inboxEvent.setProcessedAt(LocalDateTime.now());

        inboxEventRepository.save(inboxEvent);
    }

    private String toJson(ActivityLogEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Cannot serialize ActivityLogEvent",
                    exception
            );
        }
    }
}
