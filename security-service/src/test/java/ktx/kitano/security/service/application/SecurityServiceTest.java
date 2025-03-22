package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private SecurityEventStore<SystemEvent> repository;

    @Mock
    private SecurityEventProducer producer;

    @InjectMocks
    private SecurityService service;

    private SystemEvent event;

    @BeforeEach
    void setUp() {
        event = new SystemEvent(
                KtxEvent.EventType.AUTHENTICATION_FAILURE,
                KtxEvent.Level.WARNING,
                KtxEvent.Criticality.REGULAR,
                "550e8400-e29b-41d4-a716-446655440000",
                "127.0.0.1",
                "Failed login attempt",
                "security-service"
        );
    }

    @Test
    void logEvent_shouldSaveEventAndTriggerKafka() throws Exception {
        when(repository.save(event)).thenReturn(event);

        SystemEvent result = service.logEvent(event);

        assertNotNull(result);
        assertEquals(event, result);

        verify(repository, times(1)).save(event);
        verify(producer, times(1)).sendEvent(event);
    }

    @Test
    void logEvent_shouldThrowExceptionWhenEventIsNull() {
        Exception exception = assertThrows(Exception.class, () -> service.logEvent(null));
        assertInstanceOf(SystemException.class, exception);
        assertEquals("Event cannot be null", exception.getMessage());
        verifyNoInteractions(repository, producer);
    }

    @Test
    void logEvent_shouldHandleRepositoryException() throws Exception {
        when(repository.save(event)).thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () -> service.logEvent(event));
        verify(producer, never()).sendEvent(event);
    }

    @Test
    void getAllEvents_shouldReturnAllEvents() {
        when(repository.findAll()).thenReturn(List.of(event));

        List<SystemEvent> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(event, result.get(0));

        verify(repository, times(1)).findAll();
    }

    @Test
    void getEventsByUser_shouldReturnUserSpecificEvents() {
        when(repository.findByUserId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(List.of(event));

        List<SystemEvent> result = service.findByUserId("550e8400-e29b-41d4-a716-446655440000");

        assertEquals(1, result.size());
        assertEquals(event, result.get(0));

        verify(repository, times(1)).findByUserId("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void getEventById_shouldReturnEventIfExists() {
        when(repository.findById("1234")).thenReturn(event);

        SystemEvent result = service.findById("1234");

        assertNotNull(result);
        assertEquals(event, result);

        verify(repository, times(1)).findById("1234");
    }

    @Test
    void getEventById_shouldReturnNullIfNotFound() {
        when(repository.findById("9999")).thenReturn(null);

        SystemEvent result = service.findById("9999");

        assertNull(result);

        verify(repository, times(1)).findById("9999");
    }
}