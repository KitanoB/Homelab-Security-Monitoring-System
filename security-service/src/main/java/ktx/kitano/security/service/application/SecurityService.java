package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.config.SecurityProperties;
import ktx.kitano.security.service.config.UnusualBehaviourProperties;
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

    private final SecurityProperties securityProperties;

    private final UnusualBehaviourProperties usualBehaviourProperties;

    @Autowired
    public SecurityService(SecurityEventStore<SystemEvent> eventStore,
                           SecurityEventProducer producer,
                           SecurityProperties securityProperties,
                           UnusualBehaviourProperties usualBehaviourProperties) {
        this.eventStore = eventStore;
        this.producer = producer;
        this.securityProperties = securityProperties;
        this.usualBehaviourProperties = usualBehaviourProperties;
    }

    /**
     * Checks the system for security issues based on the provided event.
     * <p>
     * The system is checked for the following security issues:
     * <ul>
     *     <li>Too many failed login attempts</li>
     *     <li>Unusual IP pattern detected</li>
     *     <li>Suspicious login success after failure</li>
     *     <li>Unusual IP activity</li>
     *     <li>Too many login failures</li>
     *     <li>Too many login attempts</li>
     *     <li>Too many login failures</li>
     *   </ul>
     *   If any of these issues are detected, an exception is thrown.
     * <p>
     *
     * @param event the event to secure the system with
     * @throws Exception if the event is null or if the system cannot be secured
     */

    public void secure(SystemEvent event) throws Exception {
        if (event == null) {
            throw new SystemException("Event cannot be null");
        }

        List<SystemEvent> events = eventStore.findByUserId(event.getUserId());
        if (events == null || events.isEmpty()) return;

        if (events.size() >= 3) {
            boolean previousSuccesses = events.stream().sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                    .limit(3)
                    .allMatch(e -> KtxEvent.EventType.AUTHENTICATION_SUCCESS.equals(e.eventType()));

            if (previousSuccesses && KtxEvent.EventType.AUTHENTICATION_FAILURE.equals(event.eventType())) {
                LOGGER.info("User {} has an unusual login pattern", event.getUserId());
                throw new SystemException("User has an unusual login pattern");
            }
        }

        boolean onlyFailures = events.stream()
                .allMatch(e -> KtxEvent.EventType.AUTHENTICATION_FAILURE.equals(e.eventType()));

        if (onlyFailures && KtxEvent.EventType.AUTHENTICATION_FAILURE.equals(event.eventType())) {
            LOGGER.info("User {} has reached the maximum number of failed login attempts", event.getUserId());
            throw new SystemException("User has reached the maximum number of failed login attempts");
        }

        long ipCount = events.stream()
                .map(SystemEvent::getIpAddress)
                .distinct()
                .count();

        if (ipCount > securityProperties.getMaxIpCount()) {
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

        long recentUnusualBehaviorCount = events.stream()
                .filter(e -> e.eventType() == KtxEvent.EventType.UNUSUAL_BEHAVIOR)
                .filter(e -> e.getTimestamp() != null &&
                        e.getTimestamp().isAfter(LocalDateTime.now().minusDays(usualBehaviourProperties.getDays())))
                .count();

        if (recentUnusualBehaviorCount >= usualBehaviourProperties.getCount()) {
            LOGGER.warn("User {} has {} unusual behaviors in last {} days",
                    event.getUserId(), recentUnusualBehaviorCount, usualBehaviourProperties.getDays());

            producer.sendEvent(new SystemEvent(
                    KtxEvent.EventType.SECURITY,
                    KtxEvent.Level.ERROR,
                    KtxEvent.Criticality.CRITICAL,
                    event.getUserId(),
                    event.getIpAddress(),
                    "Repeated unusual behavior detected",
                    "security-service"
            ));
        }
    }

    /**
     * Persists the provided event and sends it to the event bus.
     *
     * @param event the event to log
     * @return the saved event
     * @throws Exception if the event is null
     */
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