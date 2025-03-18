package ktx.kitano.audit.service.application;

import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.iface.SecurityEventJpaRepository;
import ktx.kitano.audit.service.infrastructure.ISecurityEventProducer;
import ktx.kitano.audit.service.infrastructure.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    private SecurityEvent event;
    private SecurityService service;
    private SecurityEventJpaRepository repository;
    private ISecurityEventProducer producer;

    @InjectMocks
    private SecurityEventRepository securityEventRepository;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SecurityEventJpaRepository.class);
        producer = Mockito.mock(ISecurityEventProducer.class);

        event = new SecurityEvent(
                "LOGIN_FAILED",
                "testUser",
                "192.168.1.1",
                "Unauthorized login attempt"
        );

        service = new SecurityService(repository, producer);
    }

    @Test
    void logEvent_shouldSaveEventAndTriggerKafka() {

        when(repository.save(event)).thenReturn(event);

        SecurityEvent result = service.logEvent(event);

        verify(repository, times(1)).save(event);

        verify(producer, times(1)).send("security-events", event);

        assertEquals(event, result);
    }

    @Test
    void logEvent_shouldHandleNullSavedEvent() {

        when(repository.save(event)).thenReturn(null);

        SecurityEvent result = service.logEvent(event);

        verify(repository, times(1)).save(event);

        verify(producer, never()).send("security-events", event);

        assertNull(result);
    }

    @Test
    void logEvent_shouldHandleRepositoryException() {

        when(repository.save(event)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> service.logEvent(event));

        verify(producer, never()).send("security-events", event);
    }

    @Test
    void getAllEvents_shouldReturnListFromRepository() {

        when(repository.findAll()).thenReturn(java.util.List.of(event));

        var result = service.getAllEvents();

        assertEquals(1, result.size());
        assertEquals(event, result.get(0));
        verify(repository, times(1)).findAll();
    }
}