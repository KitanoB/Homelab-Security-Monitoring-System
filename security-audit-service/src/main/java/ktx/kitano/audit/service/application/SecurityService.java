package ktx.kitano.audit.service.application;

import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.iface.SecurityEventJpaRepository;
import ktx.kitano.audit.service.infrastructure.ISecurityEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SecurityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    private final SecurityEventJpaRepository service;
    private final ISecurityEventProducer producer;

    public SecurityService(SecurityEventJpaRepository service, ISecurityEventProducer producer) {
        this.service = service;
        this.producer = producer;
    }

    public SecurityEvent logEvent(SecurityEvent event) {
        LOGGER.debug("Logging event: {}", event);
        if (event == null) {
            LOGGER.error("Event cannot be null");
            return null;
        }
        SecurityEvent savedEvent = save(event);
        sendEvent(savedEvent);
        return savedEvent;
    }

    @Transactional
    private SecurityEvent save(SecurityEvent event) {
        LOGGER.debug("Saving event: {}", event);
        return service.save(event);
    }

    @Transactional(readOnly = true)
    private void sendEvent(SecurityEvent event) {
        LOGGER.debug("Sending event: {}", event);
        producer.send("security-events", event);
    }

    public List<SecurityEvent> getAllEvents() {
        LOGGER.debug("Retrieving all events");
        return service.findAll();
    }
}