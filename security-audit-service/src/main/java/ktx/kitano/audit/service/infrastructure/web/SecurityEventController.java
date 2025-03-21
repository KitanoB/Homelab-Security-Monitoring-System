package ktx.kitano.audit.service.infrastructure.web;

import ktx.kitano.audit.service.application.SecurityService;
import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Security Events.
 */
@RestController
@RequestMapping("/api/events")
public class SecurityEventController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventController.class);
    private final SecurityService eventService;

    public SecurityEventController(SecurityService eventService) {
        this.eventService = eventService;
    }

    /**
     * Logs a new security event.
     *
     * @param event The security event to log.
     * @return The logged event with HTTP 201 Created.
     */
    @PostMapping("/log")
    public ResponseEntity<SecurityEvent> logEvent(@RequestBody SecurityEvent event) {
        LOGGER.info("Received request to log event: {}", event);

        try {
            SecurityEvent savedEvent = eventService.logEvent(event);
            return ResponseEntity.status(201).body(savedEvent);
        } catch (SecurityEventException e) {
            LOGGER.error("Error logging event: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while logging event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves all security events.
     *
     * @return List of security events.
     */
    @GetMapping("/all")
    public ResponseEntity<List<SecurityEvent>> getAllEvents() {
        LOGGER.info("Received request to get all events");

        List<SecurityEvent> events = eventService.findAll();

        if (events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a specific security event by its ID.
     *
     * @param id The UUID of the event.
     * @return The found event, or HTTP 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SecurityEvent> getEventById(@PathVariable String id) {
        LOGGER.info("Received request to get event by id: {}", id);

        SecurityEvent event = eventService.findById(id);

        return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<SecurityEvent>> getEventsByUser(@PathVariable String id) {
        LOGGER.info("Received request to get events by user: {}", id);

        List<SecurityEvent> events = eventService.findByUserId(id);

        if (events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(events);
    }

    /**
     * Handles SecurityEventException globally.
     *
     * @param e The exception.
     * @return HTTP 400 Bad Request response.
     */
    @ExceptionHandler(SecurityEventException.class)
    public ResponseEntity<String> handleSecurityEventException(SecurityEventException e) {
        LOGGER.error("Handled SecurityEventException: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}