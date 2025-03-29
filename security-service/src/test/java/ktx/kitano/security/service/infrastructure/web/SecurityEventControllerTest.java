package ktx.kitano.security.service.infrastructure.web;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.application.SecurityService;
import ktx.kitano.security.service.config.SecurityProperties;
import ktx.kitano.security.service.config.UnusualBehaviourProperties;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SecurityEventControllerTest {

    private SecurityEventController controller;
    private SecurityService eventService;
    private SecurityEventStore<SystemEvent> eventStore;
    private SecurityEventProducer producer;
    private SecurityProperties securityProperties;
    private UnusualBehaviourProperties unusualBehaviourProperties;

    private SystemEvent event;

    @BeforeEach
    void setUp() {
        eventStore = mock(SecurityEventStore.class);
        producer = mock(SecurityEventProducer.class);
        securityProperties = new SecurityProperties();
        unusualBehaviourProperties = new UnusualBehaviourProperties();
        eventService = spy(new SecurityService(eventStore, producer, securityProperties, unusualBehaviourProperties));
        controller = new SecurityEventController(eventService);

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
    @DisplayName("Should return 200 and send event successfully")
    void logEvent_successful() throws Exception {
        doReturn(event).when(eventService).logEvent(event);

        ResponseEntity<SystemEvent> response = controller.logEvent(event);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(eventService, times(1)).logEvent(event);
    }

    @Test
    @DisplayName("Should return all events in descending order")
    void getAllEvents() {
        List<SystemEvent> events = List.of(event);
        when(eventStore.findAllByOrder(any())).thenReturn(events);

        ResponseEntity<List<SystemEvent>> response = controller.getAllEvents();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
    }

    @Test
    @DisplayName("Should return event by ID")
    void getEventById() {
        when(eventStore.findById(anyString())).thenReturn(event);

        ResponseEntity<SystemEvent> response = controller.getEventById("123");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(event, response.getBody());
    }

    @Test
    @DisplayName("Should return events by user ID")
    void getEventsByUser() {
        List<SystemEvent> events = List.of(event);
        when(eventStore.findByUserId(anyString())).thenReturn(events);

        ResponseEntity<List<SystemEvent>> response = controller.getEventsByUser("123");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
    }

    @Test
    @DisplayName("Should return 500 and exception message for SystemException")
    void handleSecurityEventException() {
        ResponseEntity<String> response = controller.handleSecurityEventException(new SystemException("Test exception"));

        assertNotNull(response);
        assertEquals("Test exception", response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return empty list for user with no events")
    void getEventsByUser_noEvents() {
        when(eventStore.findByUserId("nouser")).thenReturn(List.of());

        ResponseEntity<List<SystemEvent>> response = controller.getEventsByUser("nouser");

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return empty list when no events are found")
    void getAllEvents_noData() {
        when(eventStore.findAllByOrder(any())).thenReturn(List.of());

        ResponseEntity<List<SystemEvent>> response = controller.getAllEvents();

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return null body if event not found by ID")
    void getEventById_notFound() {
        when(eventStore.findById("unknown")).thenReturn(null);

        ResponseEntity<SystemEvent> response = controller.getEventById("unknown");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}