package ktx.kitano.security.service.infrastructure.repository;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SecurityEventJpaStore implements SecurityEventStore<SystemEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventJpaStore.class);

    private final SecurityEventJpaRepository jpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SecurityEventJpaStore(SecurityEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SystemEvent save(SystemEvent event) throws Exception {
        if (event == null) {
            throw new SystemException("Event cannot be null");
        }
        SystemEvent savedEvent = jpaRepository.save(event);
        LOGGER.info("Event saved: {}", savedEvent);
        return savedEvent;
    }


    @Override
    public List<SystemEvent> findByType(KtxEvent.EventType eventType) {
        List<SystemEvent> events = jpaRepository.findByEventType(eventType);
        LOGGER.trace("Found {} events", events.size());
        return events;
    }

    @Override
    public List<SystemEvent> findAllByOrder(Sort.Direction direction) {
        List<SystemEvent> events = jpaRepository.findAll(Sort.by(direction, "timestamp"));
        LOGGER.trace("Found {} events", events.size());
        return events;
    }

    @Override
    public SystemEvent findById(String id) {
        SystemEvent event = jpaRepository.findById(id).orElse(null);
        LOGGER.trace("Found event: {}", event);
        return event;
    }

    @Override
    public List<SystemEvent> findByUserId(String userId) {
        LOGGER.trace("Finding events for user: {}", userId);
        List<SystemEvent> events = jpaRepository.findByUserId(userId);
        LOGGER.trace("Found {} events for user: {}", events.size(), userId);
        return events;
    }

    @Override
    public List<SystemEvent> findByUserId(String userId, int limit) {
        LOGGER.trace("Finding {} events for user: {}", limit, userId);
        List<SystemEvent> events = entityManager.createQuery("SELECT e FROM SystemEvent e WHERE e.userId = :userId ORDER BY timestamp DESC", SystemEvent.class)
                .setParameter("userId", userId)
                .setMaxResults(limit)
                .getResultList();
        LOGGER.trace("Found {} events for user: {}", events.size(), userId);
        return events;
    }


}