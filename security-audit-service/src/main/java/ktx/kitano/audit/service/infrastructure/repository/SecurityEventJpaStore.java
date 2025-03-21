package ktx.kitano.audit.service.infrastructure.repository;

import com.kitano.iface.model.KtxEvent;
import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SecurityEventJpaStore implements SecurityEventStore<SecurityEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityEventJpaStore.class);

    private final SecurityEventJpaRepository jpaRepository;

    public SecurityEventJpaStore(SecurityEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SecurityEvent save(SecurityEvent event) throws Exception {
        if (event == null) {
            throw new SecurityEventException("Event cannot be null");
        }
        SecurityEvent savedEvent = jpaRepository.save(event);
        LOGGER.info("Event saved: {}", savedEvent);
        return savedEvent;
    }


    @Override
    public List<SecurityEvent> findByType(KtxEvent.EventType eventType) {
        List<SecurityEvent> events = jpaRepository.findByEventType(eventType);
        LOGGER.trace("Found {} events", events.size());
        return events;
    }

    @Override
    public List<SecurityEvent> findAll() {
        List<SecurityEvent> events = jpaRepository.findAll();
        LOGGER.trace("Found {} events", events.size());
        return events;
    }

    @Override
    public SecurityEvent findById(String id) {
        SecurityEvent event = jpaRepository.findById(id).orElse(null);
        LOGGER.trace("Found event: {}", event);
        return event;
    }

    @Override
    public List<SecurityEvent> findByUserId(String userId) {
        LOGGER.trace("Finding events for user: {}", userId);
        List<SecurityEvent> events = jpaRepository.findByUserId(userId);
        LOGGER.trace("Found {} events for user: {}", events.size(), userId);
        return events;
    }
}