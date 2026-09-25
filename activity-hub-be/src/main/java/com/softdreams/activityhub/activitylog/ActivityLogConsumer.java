package com.softdreams.activityhub.activitylog;

import com.softdreams.activityhub.infrastructure.inbox.InboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.softdreams.activityhub.configuration.RabbitMQConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogConsumer {

    private final InboxService inboxService;

    @RabbitListener(queues = RabbitMQConfig.ACTIVITY_QUEUE)
    public void consume(ActivityLogEvent event) {

        log.info(
                "Received ActivityLogEvent: {}",
                event.getEventId()
        );

        inboxService.process(event);
    }
}
