package com.softdreams.activityhub.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.softdreams.activityhub.configuration.RabbitMQConfig;
import com.softdreams.activityhub.dto.ActivityLogEvent;
import com.softdreams.activityhub.service.ActivityLogService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityLogConsumer {

    private final ActivityLogService activityLogService;

    @RabbitListener(queues = RabbitMQConfig.ACTIVITY_QUEUE)
    public void consume(ActivityLogEvent event) {

        activityLogService.save(event);
    }
}
