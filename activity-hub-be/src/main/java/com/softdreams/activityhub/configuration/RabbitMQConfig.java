package com.softdreams.activityhub.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACTIVITY_EXCHANGE = "activity.exchange";
    public static final String ACTIVITY_QUEUE = "activity.log.queue";
    public static final String ACTIVITY_ROUTING_KEY = "activity.log";

    @Bean
    public DirectExchange activityExchange() {
        return new DirectExchange(ACTIVITY_EXCHANGE);
    }

    @Bean
    public Queue activityQueue() {
        return new Queue(ACTIVITY_QUEUE, true);
    }

    @Bean
    public Binding activityBinding(Queue activityQueue, DirectExchange activityExchange) {
        return BindingBuilder.bind(activityQueue).to(activityExchange).with(ACTIVITY_ROUTING_KEY);
    }
}
