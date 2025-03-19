package ktx.kitano.audit.service.infrastructure.repository;

import com.kitano.iface.KtxEvent;
import com.kitano.iface.KtxEventService;
import com.kitano.iface.KtxJpaEventRepository;
import ktx.kitano.audit.service.domain.SecurityEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Generic repository implementation for KtxEvent-based entities.
 *
 * @param <T> Type of event, must extend KtxEvent<?>
 */
@Repository
public class SecurityEventRepository<T extends KtxEvent<?>> implements KtxEventService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventRepository.class);
    private final KtxJpaEventRepository<T> jpaRepository;

    public SecurityEventRepository(KtxJpaEventRepository<T> jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public T save(T event) throws SecurityEventException {
        LOGGER.debug("Saving event: {}", event);
        T savedEvent = jpaRepository.save(event);

        // for testing purposes
        if (savedEvent == null) {
            throw new SecurityEventException("Event could not be saved");
        }

        return savedEvent;
    }

    @Override
    public List<T> findByType(KtxEvent.EventType eventType) {
        LOGGER.debug("Retrieving events by type: {}", eventType);
        return jpaRepository.findByEventType(eventType);
    }

    @Override
    public List<T> findAll() {
        LOGGER.debug("Retrieving all events");
        return jpaRepository.findAll();
    }

    @Override
    public T findById(String id) {
        LOGGER.debug("Retrieving event by id: {}", id);
        return jpaRepository.findById(id).orElse(null);
    }

    @Override
    public List<T> findByUserId(String id) {
        LOGGER.debug("Retrieving events by user id: {}", id);
        return jpaRepository.findByUserId(id);
    }
}