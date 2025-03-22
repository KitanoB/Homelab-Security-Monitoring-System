package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.kitano.iface.model.KtxEvent.*;
import static org.mockito.Mockito.*;

class SecurityEventConsumerTest {

    private SecurityService securityService;
    private SecurityEventConsumer consumer;

    @BeforeEach
    void setup() {
        securityService = mock(SecurityService.class);
        consumer = new SecurityEventConsumer(securityService);
    }

    @Test
    void consume_shouldCallSecureAndLogEvent() throws Exception {
        SystemEvent event = createEvent();
        ConsumerRecord<String, SystemEvent> systemEventConsumerRecord = new ConsumerRecord<>("auth-events", 0, 0, null, event);

        consumer.consume(systemEventConsumerRecord);

        verify(securityService, times(1)).secure(event);
        verify(securityService, times(1)).logEvent(event);
    }

    @Test
    void consume_shouldHandleSystemException() throws Exception {
        SystemEvent event = createEvent();
        ConsumerRecord<String, SystemEvent> systemEventConsumerRecord = new ConsumerRecord<>("auth-events", 0, 0, null, event);

        doThrow(new SystemException("Test failure")).when(securityService).secure(event);

        consumer.consume(systemEventConsumerRecord);

        verify(securityService, times(1)).secure(event);
        verify(securityService, never()).logEvent(any());
    }

    @Test
    void consume_shouldHandleGenericException() throws Exception {
        SystemEvent event = createEvent();
        ConsumerRecord<String, SystemEvent> systemEventConsumerRecord = new ConsumerRecord<>("auth-events", 0, 0, null, event);

        doThrow(new RuntimeException("Unexpected")).when(securityService).secure(event);

        consumer.consume(systemEventConsumerRecord);

        verify(securityService, times(1)).secure(event);
        verify(securityService, never()).logEvent(any());
    }

    private SystemEvent createEvent() {
        return SystemEvent.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .eventType(EventType.AUTHENTICATION_SUCCESS)
                .level(Level.INFO)
                .criticality(Criticality.REGULAR)
                .userId("user-123")
                .ipAddress("127.0.0.1")
                .message("Test event")
                .source("auth-service")
                .build();
    }
}