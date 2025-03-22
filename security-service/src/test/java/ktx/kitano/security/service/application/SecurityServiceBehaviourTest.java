package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.config.SecurityProperties;
import ktx.kitano.security.service.config.UnusualBehaviourProperties;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_FAILURE;
import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
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
    void secure_shouldSendSecurityEventIfTooManyUnusualBehaviorInPeriod() throws Exception {
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

    @Test
    @DisplayName("Should throw when user has too many distinct IP addresses")
    void secure_shouldThrowOnTooManyDistinctIps() {
        securityProperties.setMaxIpCount(2);

        List<SystemEvent> events = List.of(
                eventWithIp("127.0.0.1"),
                eventWithIp("127.0.0.2"),
                eventWithIp("127.0.0.3")
        );

        when(store.findByUserId(userId)).thenReturn(events);

        SystemEvent newEvent = eventWithIp("127.0.0.4");

        SystemException ex = assertThrows(SystemException.class, () -> service.secure(newEvent));
        assertTrue(ex.getMessage().contains("Unusual IP pattern detected"));
    }

    @Test
    @DisplayName("Should send UNUSUAL_BEHAVIOR when success follows a failure")
    void secure_shouldSendUnusualBehaviorOnSuccessAfterFailure() throws Exception {
        List<SystemEvent> events = List.of(
                eventWithType(AUTHENTICATION_FAILURE),
                eventWithType(AUTHENTICATION_SUCCESS)
        );

        when(store.findByUserId(userId)).thenReturn(events);

        SystemEvent newEvent = eventWithType(AUTHENTICATION_SUCCESS);

        service.secure(newEvent);

        verify(producer, times(1)).sendEvent(argThat(ev ->
                ev.getEventType() == KtxEvent.EventType.UNUSUAL_BEHAVIOR &&
                        ev.getMessage().contains("Suspicious login success")
        ));
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