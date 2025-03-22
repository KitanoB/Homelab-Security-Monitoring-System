package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.config.SecurityProperties;
import ktx.kitano.security.service.config.UsualBehaviourProperties;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_FAILURE;
import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceBehaviourTest {

    private SecurityEventStore<SystemEvent> store;
    private SecurityEventProducer producer;
    private SecurityService service;
    private SecurityProperties securityProperties;
    private UsualBehaviourProperties usualBehaviourProperties;
    private long timeDifference = 1;

    private final String userId = "user-123";

    @BeforeEach
    void setup() {
        store = mock(SecurityEventStore.class);
        producer = mock(SecurityEventProducer.class);
        securityProperties = new SecurityProperties();
        usualBehaviourProperties = new UsualBehaviourProperties();
        service = new SecurityService(store, producer, securityProperties, usualBehaviourProperties);
    }

    @Test
    void secure_shouldThrowIfTooManyFailures() {
        List<SystemEvent> events = List.of(
                event(AUTHENTICATION_FAILURE),
                event(AUTHENTICATION_FAILURE),
                event(AUTHENTICATION_FAILURE)
        );

        when(store.findByUserId(anyString())).thenReturn(events);

        SystemEvent newEvent = event(AUTHENTICATION_FAILURE);

        SystemException exception = assertThrows(SystemException.class, () -> service.secure(newEvent));
        assertTrue(exception.getMessage().contains("maximum number"));
    }

    @Test
    void secure_shouldThrowOnUnusualBehavior() {
        List<SystemEvent> events = List.of(
                event(AUTHENTICATION_FAILURE),
                event(AUTHENTICATION_SUCCESS),
                event(AUTHENTICATION_SUCCESS),
                event(AUTHENTICATION_SUCCESS)
        );

        when(store.findByUserId(anyString())).thenReturn(events);

        SystemEvent newEvent = event(AUTHENTICATION_FAILURE);

        SystemException exception = assertThrows(SystemException.class, () -> service.secure(newEvent));
        assertTrue(exception.getMessage().contains("unusual login pattern"));
    }

    @Test
    void secure_shouldAllowNormalBehavior() throws Exception {

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
    void secure_shouldHandleEmptyEventList() throws Exception {
        when(store.findByUserId(userId)).thenReturn(List.of());

        SystemEvent newEvent = event(AUTHENTICATION_FAILURE);

        assertDoesNotThrow(() -> service.secure(newEvent));
    }

    @Test
    void secure_shouldSendSecurityEventIfTooManyUnusualBehaviorInPeriod() throws Exception {
        // Simule un seuil dépassé
        usualBehaviourProperties.setCount(3);
        usualBehaviourProperties.setDays(7);

        List<SystemEvent> events = List.of(
                event(KtxEvent.EventType.UNUSUAL_BEHAVIOR),
                event(KtxEvent.EventType.UNUSUAL_BEHAVIOR),
                event(KtxEvent.EventType.UNUSUAL_BEHAVIOR)
        );

        when(store.findByUserId(userId)).thenReturn(events);

        SystemEvent newEvent = event(KtxEvent.EventType.AUTHENTICATION_SUCCESS);

        service.secure(newEvent);

        verify(producer, times(1)).sendEvent(argThat(e ->
                e.eventType() == KtxEvent.EventType.SECURITY &&
                        e.getUserId().equals(userId) &&
                        e.getMessage().contains("Repeated unusual behavior")
        ));
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

    private SystemEvent event(KtxEvent.EventType type) {
        return new SystemEvent(
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusMinutes(++timeDifference),
                type,
                KtxEvent.Level.INFO,
                KtxEvent.Criticality.REGULAR,
                userId,
                "localhost",
                "Test event",
                "auth-service"
        );
    }
}