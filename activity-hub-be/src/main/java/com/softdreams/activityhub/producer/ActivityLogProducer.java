package com.softdreams.activityhub.producer;

import com.softdreams.activityhub.configuration.RabbitMQConfig;
import com.softdreams.activityhub.dto.ActivityLogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityLogProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(ActivityLogEvent event) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACTIVITY_EXCHANGE,
                RabbitMQConfig.ACTIVITY_ROUTING_KEY,
                event
        );
    }
}
