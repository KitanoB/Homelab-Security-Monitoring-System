package ktx.kitano.audit.service.infrastructure;

import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;
import ktx.kitano.audit.service.iface.SecurityEventJpaRepository;
import ktx.kitano.audit.service.iface.SecurityEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SecurityEventRepository implements SecurityEventService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityEventRepository.class);
    private final SecurityEventJpaRepository jpaRepository;

    public SecurityEventRepository(SecurityEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SecurityEvent save(SecurityEvent event) throws SecurityEventException {
        if (event == null) {
            throw new SecurityEventException("Event cannot be null");
        }

        LOGGER.debug("Saving event: {}", event);
        SecurityEvent savedEvent = jpaRepository.save(event);

        return savedEvent;
    }

    @Override
    public List<SecurityEvent> findByType(String eventType) {
        LOGGER.debug("Retrieving events by type: {}", eventType);
        List<SecurityEvent> events = jpaRepository.findByEventType(eventType);
        LOGGER.debug("Retrieved events by type:  {}", events);
        return events;
    }

    @Override
    public List<SecurityEvent> findAll() {
        LOGGER.debug("Retrieving all events");
        List<SecurityEvent> events = jpaRepository.findAll();
        LOGGER.debug("Retrieved events: {}", events);
        return events;
    }

    @Override
    public SecurityEvent findById(UUID id) {
        LOGGER.debug("Retrieving event by id: {}", id);
        SecurityEvent event = jpaRepository.findById(id).orElse(null);
        LOGGER.debug("Retrieved event: {}", event);
        return event;
    }
}