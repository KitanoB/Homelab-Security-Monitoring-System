package ktx.kitano.audit.service.application;

import com.kitano.iface.KtxEvent;
import ktx.kitano.audit.service.domain.HomeLabUser;
import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;
import ktx.kitano.audit.service.infrastructure.repository.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    private SecurityEvent event;
    private SecurityService service;
    private SecurityEventRepository<SecurityEvent> repository;
    private HomeLabUser user;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SecurityEventRepository.class);
        user = Mockito.mock(HomeLabUser.class);

        event = new SecurityEvent(
                KtxEvent.EventType.AUTH,
                KtxEvent.Level.WARNING,
                KtxEvent.Criticality.REGULAR,
                UUID.randomUUID().toString(),
                "127.0.0.1",
                "Failed login attempt"
        );

        service = new SecurityService(repository);
    }

    @Test
    void save_shouldSaveEventSuccessfully() throws Exception {
        when(repository.save(event)).thenReturn(event);

        SecurityEvent result = service.save(event);

        verify(repository, times(1)).save(event);
        assertEquals(event, result);
    }

    @Test
    void save_shouldThrowExceptionWhenEventIsNull() {
        assertThrows(SecurityEventException.class, () -> service.save(null));
        verifyNoInteractions(repository);
    }

    @Test
    void save_shouldHandleRepositoryException() throws SecurityEventException {
        when(repository.save(event)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> service.save(event));

        verify(repository, times(1)).save(event);
    }

    @Test
    void findAll_shouldReturnListOfEvents() {
        when(repository.findAll()).thenReturn(List.of(event));

        List<SecurityEvent> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(event, result.get(0));
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoEvents() {
        when(repository.findAll()).thenReturn(List.of());

        List<SecurityEvent> result = service.findAll();

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findByType_shouldReturnMatchingEvents() {
        when(repository.findByType(KtxEvent.EventType.AUTH)).thenReturn(List.of(event));

        List<SecurityEvent> result = service.findByType(KtxEvent.EventType.AUTH);

        assertEquals(1, result.size());
        assertEquals(event, result.get(0));
        verify(repository, times(1)).findByType(KtxEvent.EventType.AUTH);
    }

    @Test
    void findByType_shouldReturnEmptyListWhenNoMatchingEvents() {
        when(repository.findByType(KtxEvent.EventType.AUTH)).thenReturn(List.of());

        List<SecurityEvent> result = service.findByType(KtxEvent.EventType.AUTH);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findByType(KtxEvent.EventType.AUTH);
    }

    @Test
    void findById_shouldReturnEventIfExists() {
        UUID eventId = UUID.randomUUID();
        when(repository.findById(eventId.toString())).thenReturn(event);

        SecurityEvent result = service.findById(eventId.toString());

        assertNotNull(result);
        assertEquals(event, result);
        verify(repository, times(1)).findById(eventId);
    }

    @Test
    void findById_shouldReturnNullIfNotFound() {
        UUID eventId = UUID.randomUUID();
        when(repository.findById(eventId.toString())).thenReturn(null);

        SecurityEvent result = service.findById(eventId.toString());

        assertNull(result);
        verify(repository, times(1)).findById(eventId.toString());
    }
}