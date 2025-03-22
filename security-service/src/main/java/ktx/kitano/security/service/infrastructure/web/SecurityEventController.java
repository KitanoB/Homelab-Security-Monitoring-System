package ktx.kitano.security.service.infrastructure.web;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import ktx.kitano.security.service.application.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing System Events.
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
    public ResponseEntity<SystemEvent> logEvent(@RequestBody SystemEvent event) {
        LOGGER.info("Received request to log event: {}", event);

        try {
            SystemEvent savedEvent = eventService.logEvent(event);
            return ResponseEntity.status(201).body(savedEvent);
        } catch (SystemException e) {
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
    public ResponseEntity<List<SystemEvent>> getAllEvents() {
        LOGGER.info("Received request to get all events");

        List<SystemEvent> events = eventService.findAllByOrder(Sort.Direction.DESC);

        if (events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(events);
    }

    /**
     * Retrieves a specific system event by its ID.
     *
     * @param id The UUID of the event.
     * @return The found event, or HTTP 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SystemEvent> getEventById(@PathVariable String id) {
        LOGGER.info("Received request to get event by id: {}", id);

        SystemEvent event = eventService.findById(id);

        return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<SystemEvent>> getEventsByUser(@PathVariable String id) {
        LOGGER.info("Received request to get events by user: {}", id);

        List<SystemEvent> events = eventService.findByUserId(id);

        if (events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(events);
    }

    /**
     * Handles SecuritySystemException globally.
     *
     * @param e The exception.
     * @return HTTP 400 Bad Request response.
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<String> handleSecurityEventException(SystemException e) {
        LOGGER.error("Handled SecurityEventException: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}