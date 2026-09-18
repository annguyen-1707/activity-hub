package com.softdreams.activityhub.consumer;

import com.softdreams.activityhub.configuration.RabbitMQConfig;
import com.softdreams.activityhub.dto.ActivityLogEvent;
import com.softdreams.activityhub.service.ActivityLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityLogConsumer {

    private final ActivityLogService activityLogService;

    @RabbitListener(queues = RabbitMQConfig.ACTIVITY_QUEUE)
    public void consume(ActivityLogEvent event) {

        activityLogService.save(event);
    }
}
