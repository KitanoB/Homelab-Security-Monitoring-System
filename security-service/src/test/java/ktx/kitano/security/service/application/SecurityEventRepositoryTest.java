package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventJpaRepository;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventJpaStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityEventRepositoryTest {

    @Mock
    private SecurityEventJpaRepository jpaRepository;

    @InjectMocks
    private SecurityEventJpaStore repository;

    private SystemEvent event;

    @BeforeEach
    void setUp() {
        event = SystemEvent.builder()
                .eventType(KtxEvent.EventType.AUTHENTICATION_FAILURE)
                .level(KtxEvent.Level.WARNING)
                .criticality(KtxEvent.Criticality.REGULAR)
                .userId("550e8400-e29b-41d4-a716-446655440000")
                .ipAddress("127.0.0.1")
                .message("Failed login attempt")
                .source("security-service")
                .build();
    }

    @Test
    void save_shouldPersistEvent() throws Exception {
        when(jpaRepository.save(event)).thenReturn(event);

        SystemEvent savedEvent = repository.save(event);

        assertNotNull(savedEvent);
        assertEquals(event, savedEvent);
        verify(jpaRepository, times(1)).save(event);
    }

    @Test
    void save_shouldThrowExceptionOnNullEvent() {
        SystemException exception = assertThrows(SystemException.class, () -> repository.save(null));
        assertEquals("Event cannot be null", exception.getMessage());
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void findByType_shouldReturnMatchingEvents() {
        when(jpaRepository.findByEventType(KtxEvent.EventType.AUTHENTICATION_SUCCESS)).thenReturn(List.of(event));

        List<SystemEvent> result = repository.findByType(KtxEvent.EventType.AUTHENTICATION_SUCCESS);

        assertEquals(1, result.size());
        assertEquals(event, result.get(0));
        verify(jpaRepository, times(1)).findByEventType(KtxEvent.EventType.AUTHENTICATION_SUCCESS);
    }

    @Test
    void findById_shouldReturnEvent() {
        when(jpaRepository.findById("1234")).thenReturn(Optional.of(event));

        SystemEvent result = repository.findById("1234");

        assertNotNull(result);
        assertEquals(event, result);
        verify(jpaRepository, times(1)).findById("1234");
    }

    @Test
    void findById_shouldReturnNullIfNotFound() {
        when(jpaRepository.findById("9999")).thenReturn(Optional.empty());

        SystemEvent result = repository.findById("9999");

        assertNull(result);
        verify(jpaRepository, times(1)).findById("9999");
    }

    @Test
    void findByUserId_shouldReturnEventsForUser() {
        when(jpaRepository.findByUserId("550e8400-e29b-41d4-a716-446655440000")).thenReturn(List.of(event));

        List<SystemEvent> result = repository.findByUserId("550e8400-e29b-41d4-a716-446655440000");

        assertEquals(1, result.size());
        assertEquals(event, result.get(0));
        verify(jpaRepository, times(1)).findByUserId("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void findByUserId_shouldReturnEmptyListIfNotFound() {
        when(jpaRepository.findByUserId("unknown-user")).thenReturn(List.of());

        List<SystemEvent> result = repository.findByUserId("unknown-user");

        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByUserId("unknown-user");
    }
}