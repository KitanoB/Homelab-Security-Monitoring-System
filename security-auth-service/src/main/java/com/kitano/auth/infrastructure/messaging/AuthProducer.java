package com.kitano.auth.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;

import com.kitano.core.model.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthProducer.class);
    private final KafkaTemplate<String, SystemEvent> kafka;

    private static final String TOPIC = "auth-events";

    public AuthProducer(KafkaTemplate<String, SystemEvent> kafka) {
        this.kafka = kafka;
    }

    public boolean sendEvent(SystemEvent event) {
        try {
            kafka.send(TOPIC, event);
            LOGGER.info("Event sent to Kafka: {}", event);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send event to Kafka", e);
            return false;
        }
    }
}