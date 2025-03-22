package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.config.SecurityProperties;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_FAILURE;
import static com.kitano.iface.model.KtxEvent.EventType.AUTHENTICATION_SUCCESS;


/**
 * Service class for managing Security Events.
 */
@Service
public class SecurityService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    private final SecurityEventStore<SystemEvent> eventStore;
    private final SecurityEventProducer producer;
    private final SecurityProperties properties;

    @Autowired
    public SecurityService(SecurityEventStore<SystemEvent> eventStore, SecurityEventProducer producer, SecurityProperties properties) {
        this.eventStore = eventStore;
        this.producer = producer;
        this.properties = properties;
    }

    public void secure(SystemEvent event) throws Exception {
        if (event == null) {
            throw new SystemException("Event cannot be null");
        }

        List<SystemEvent> events = eventStore.findByUserId(event.getUserId());

        long recentFailures = events.stream()
                .filter(e -> e.eventType() == AUTHENTICATION_FAILURE)
                .filter(e -> e.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(properties.getFailureWindowMinutes())))
                .count();

        if (recentFailures >= properties.getMaxFailures()) {
            LOGGER.warn("User {} has too many login failures", event.getUserId());
            throw new SystemException("Too many failed login attempts");
        }

        long ipCount = events.stream()
                .map(SystemEvent::getIpAddress)
                .distinct()
                .count();

        if (ipCount > 3) {
            LOGGER.warn("Unusual IP activity for user {}", event.getUserId());
            throw new SystemException("Unusual IP pattern detected");
        }

        if (events.size() >= 2 &&
                events.get(events.size() - 1).eventType() == AUTHENTICATION_SUCCESS &&
                events.get(events.size() - 2).eventType() == AUTHENTICATION_FAILURE) {
            LOGGER.info("Suspicious login success after failure for user {}", event.getUserId());
            producer.sendEvent(new SystemEvent(
                    KtxEvent.EventType.UNUSUAL_BEHAVIOR,
                    KtxEvent.Level.WARNING,
                    KtxEvent.Criticality.REGULAR,
                    event.getUserId(),
                    event.getIpAddress(),
                    "Suspicious login success after failure",
                    "security-service"
            ));
        }
    }

    public SystemEvent logEvent(SystemEvent event) throws Exception {
        if (event == null) {
            throw new SystemException("Event cannot be null");
        }
        SystemEvent savedEvent = eventStore.save(event);
        producer.sendEvent(savedEvent);
        return savedEvent;
    }

    public List<SystemEvent> findByType(KtxEvent.EventType eventType) {
        return eventStore.findByType(eventType);
    }

    public List<SystemEvent> findAll() {
        return eventStore.findAll();
    }

    public SystemEvent findById(String id) {
        return eventStore.findById(id);
    }

    public List<SystemEvent> findByUserId(String id) {
        return eventStore.findByUserId(id);
    }
}