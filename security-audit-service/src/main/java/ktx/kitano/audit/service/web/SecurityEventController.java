package ktx.kitano.audit.service.web;

import ktx.kitano.audit.service.application.SecurityService;
import ktx.kitano.audit.service.domain.SecurityEvent;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class SecurityEventController {
    private final SecurityService eventService;

    public SecurityEventController(SecurityService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/log")
    public SecurityEvent logEvent(@RequestBody SecurityEvent event) {
        return eventService.logEvent(event);
    }

    @GetMapping("/all")
    public List<SecurityEvent> getAllEvents() {
        return eventService.getAllEvents();
    }
}