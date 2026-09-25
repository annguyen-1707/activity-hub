package com.softdreams.activityhub.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softdreams.activityhub.activitylog.ActivityLogEvent;
import com.softdreams.activityhub.enums.OutboxStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void save(ActivityLogEvent event) {

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventId(event.getEventId());
        outboxEvent.setEventType(
                event.getEventType().name()
        );
        outboxEvent.setAggregateType(
                event.getTargetType().name()
        );
        outboxEvent.setAggregateId(
                event.getTargetId()
        );
        outboxEvent.setStatus(OutboxStatus.PENDING);
        outboxEvent.setCreatedAt(LocalDateTime.now());

        try {
            outboxEvent.setPayload(
                    objectMapper.writeValueAsString(event)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Cannot serialize event",
                    e
            );
        }

        outboxEventRepository.save(outboxEvent);
    }
}
