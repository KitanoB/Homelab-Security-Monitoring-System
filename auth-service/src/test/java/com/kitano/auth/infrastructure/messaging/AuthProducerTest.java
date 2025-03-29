package com.kitano.auth.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import com.kitano.iface.model.KtxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.*;

class AuthProducerTest {

    private KafkaTemplate<String, SystemEvent> kafkaTemplate;
    private AuthProducer authProducer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        authProducer = new AuthProducer(kafkaTemplate);
    }

    @Test
    void sendEvent_shouldSendMessageToKafka() {
        // Given
        SystemEvent event = SystemEvent.builder()
                .userId("123")
                .eventType(KtxEvent.EventType.AUTHENTICATION_SUCCESS)
                .level(KtxEvent.Level.INFO)
                .criticality(KtxEvent.Criticality.REGULAR)
                .message("Login successful")
                .source("auth-service")
                .build();

        // When
        authProducer.sendEvent(event);

        // Then
        verify(kafkaTemplate, times(1)).send("auth-events", event);
    }
}