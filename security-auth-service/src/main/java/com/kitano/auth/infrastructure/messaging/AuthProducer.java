package com.kitano.auth.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;

@Component
public class AuthProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthProducer.class);

    private final KafkaOperations<String, SystemEvent> kafka;

    public AuthProducer(KafkaOperations<String, SystemEvent> kafka) {
        this.kafka = kafka;
    }

    public boolean sendEvent(SystemEvent event) {
        LOGGER.info("Sending event: {}", event);
        return true;
    }


}
