package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.config.SecurityProperties;
import ktx.kitano.security.service.config.UnusualBehaviourProperties;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_FAILURE;
import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class SecurityServiceBehaviourTest {

    private final String userId = "user-123";
    private SecurityEventStore<SystemEvent> store;
    private SecurityEventProducer producer;
    private SecurityService service;
    private SecurityProperties securityProperties;
    private UnusualBehaviourProperties usualBehaviourProperties;
    private long timeDifference = 1;

    @BeforeEach
    void setup() {
        store = mock(SecurityEventStore.class);
        producer = mock(SecurityEventProducer.class);
        securityProperties = new SecurityProperties();
        usualBehaviourProperties = new UnusualBehaviourProperties();
        service = new SecurityService(store, producer, securityProperties, usualBehaviourProperties);
    }

    @Test
    void secure_shouldAllowNormalBehavior() {

        List<SystemEvent> events = List.of(
                event(AUTHENTICATION_SUCCESS),
                event(AUTHENTICATION_FAILURE),
                event(KtxEvent.EventType.USER_ACTION)
        );

        when(store.findByUserId(userId)).thenReturn(events);

        SystemEvent newEvent = event(KtxEvent.EventType.USER_ACTION);

        assertDoesNotThrow(() -> service.secure(newEvent));
    }

    @Test
    void secure_shouldHandleEmptyEventList() {
        when(store.findByUserId(userId)).thenReturn(List.of());

        SystemEvent newEvent = event(AUTHENTICATION_FAILURE);

        assertDoesNotThrow(() -> service.secure(newEvent));
    }


    @Test
    void secure_shouldNotSendSecurityEventIfUnusualBehaviorBelowThreshold() throws Exception {
        usualBehaviourProperties.setCount(5);
        usualBehaviourProperties.setDays(7);

        List<SystemEvent> events = List.of(
                event(KtxEvent.EventType.UNUSUAL_BEHAVIOR),
                event(KtxEvent.EventType.UNUSUAL_BEHAVIOR)
        );

        when(store.findByUserId(userId)).thenReturn(events);

        SystemEvent newEvent = event(KtxEvent.EventType.AUTHENTICATION_SUCCESS);

        service.secure(newEvent);

        verify(producer, never()).sendEvent(argThat(e -> e.eventType() == KtxEvent.EventType.SECURITY));
    }

    private SystemEvent eventWithIp(String ip) {
        return SystemEvent.builder()
                .eventType(AUTHENTICATION_SUCCESS)
                .timestamp(LocalDateTime.now())
                .level(KtxEvent.Level.INFO)
                .criticality(KtxEvent.Criticality.REGULAR)
                .userId(userId)
                .ipAddress(ip)
                .message("Test")
                .source("auth-service")
                .build();
    }

    private SystemEvent eventWithType(KtxEvent.EventType type) {
        return SystemEvent.builder()
                .eventType(type)
                .level(KtxEvent.Level.INFO)
                .criticality(KtxEvent.Criticality.REGULAR)
                .userId(userId)
                .ipAddress("127.0.0.1")
                .message("Test")
                .source("auth-service")
                .build();
    }


    private SystemEvent event(KtxEvent.EventType type) {
        return SystemEvent.builder()
                .eventType(type)
                .level(KtxEvent.Level.INFO)
                .criticality(KtxEvent.Criticality.REGULAR)
                .userId(userId)
                .ipAddress("localhost")
                .message("Test event")
                .source("auth-service")
                .timestamp(LocalDateTime.now().plusMinutes(++timeDifference))
                .build();
    }


}