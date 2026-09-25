package com.softdreams.activityhub.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softdreams.activityhub.activitylog.ActivityLogEvent;
import com.softdreams.activityhub.activitylog.ActivityLogProducer;
import com.softdreams.activityhub.enums.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ActivityLogProducer producer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusInOrderByCreatedAtAsc(
                                List.of(
                                        OutboxStatus.PENDING,
                                        OutboxStatus.FAILED
                                )
                        );
        for (OutboxEvent outboxEvent : events) {
            try {
                ActivityLogEvent event =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                ActivityLogEvent.class
                        );
                // Gửi RabbitMQ ở đây
                producer.send(event);
                // Chỉ đánh dấu SENT sau khi publish
                outboxEvent.setStatus(OutboxStatus.SENT);
                outboxEvent.setPublishedAt(
                        LocalDateTime.now()
                );
                outboxEventRepository.save(outboxEvent);
            } catch (Exception exception) {
                outboxEvent.setStatus(OutboxStatus.FAILED);
                outboxEvent.setRetryCount(
                        outboxEvent.getRetryCount() + 1
                );
                outboxEvent.setLastError(
                        exception.getMessage()
                );

                outboxEventRepository.save(outboxEvent);

                log.error(
                        "Failed to publish eventId={}",
                        outboxEvent.getEventId(),
                        exception
                );
            }
        }
    }
}
