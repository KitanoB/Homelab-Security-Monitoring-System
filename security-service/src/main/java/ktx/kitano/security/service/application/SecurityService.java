package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.iface.model.KtxEvent;
import ktx.kitano.security.service.config.SecurityProperties;
import ktx.kitano.security.service.config.UnusualBehaviourProperties;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

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
     */

    public void secure(SystemEvent event) {

        List<SystemEvent> events = eventStore.findByUserId(event.getUserId());
        if (events == null || events.isEmpty()) return;

        if (events.size() >= 3) {
            boolean previousSuccesses = events.stream().sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                    .limit(3)
                    .allMatch(e -> KtxEvent.EventType.AUTHENTICATION_SUCCESS.equals(e.eventType()));

            if (previousSuccesses && KtxEvent.EventType.AUTHENTICATION_FAILURE.equals(event.eventType())) {
                LOGGER.info("User {} has an unusual login pattern", event.getUserId());
                SystemEvent.SystemEventBuilder eventBuilder = SystemEvent.builder()
                        .eventType(KtxEvent.EventType.UNUSUAL_BEHAVIOR)
                        .level(KtxEvent.Level.WARNING)
                        .criticality(KtxEvent.Criticality.REGULAR)
                        .userId(event.getUserId())
                        .ipAddress(event.getIpAddress())
                        .message("Unusual login pattern detected")
                        .source("security-service");
                logEvent(eventBuilder.build());
            }
        }

        boolean onlyFailures = events.stream()
                .allMatch(e -> KtxEvent.EventType.AUTHENTICATION_FAILURE.equals(e.eventType()));

        if (onlyFailures && KtxEvent.EventType.AUTHENTICATION_FAILURE.equals(event.eventType())) {
            LOGGER.info("User {} has reached the maximum number of failed login attempts", event.getUserId());
            SystemEvent.SystemEventBuilder eventBuilder = SystemEvent.builder()
                    .eventType(KtxEvent.EventType.UNUSUAL_BEHAVIOR)
                    .level(KtxEvent.Level.WARNING)
                    .criticality(KtxEvent.Criticality.REGULAR)
                    .userId(event.getUserId())
                    .ipAddress(event.getIpAddress())
                    .message("Maximum number of failed login attempts reached")
                    .source("security-service");
            logEvent(eventBuilder.build());
        }

        long ipCount = events.stream()
                .map(SystemEvent::getIpAddress)
                .distinct()
                .count();

        if (ipCount > securityProperties.getMaxIpCount()) {
            LOGGER.warn("Unusual IP activity for user {}", event.getUserId());
            SystemEvent.SystemEventBuilder eventBuilder = SystemEvent.builder()
                    .eventType(KtxEvent.EventType.UNUSUAL_BEHAVIOR)
                    .level(KtxEvent.Level.WARNING)
                    .criticality(KtxEvent.Criticality.REGULAR)
                    .userId(event.getUserId())
                    .ipAddress(event.getIpAddress())
                    .message("Unusual IP activity detected")
                    .source("security-service");

            logEvent(eventBuilder.build());
        }

        SystemEvent.SystemEventBuilder eventBuilder = SystemEvent.builder();

        if (events.size() >= 2 &&
                events.get(events.size() - 1).eventType() == AUTHENTICATION_SUCCESS &&
                events.get(events.size() - 2).eventType() == AUTHENTICATION_FAILURE) {
            LOGGER.info("Suspicious login success after failure for user {}", event.getUserId());
            eventBuilder.eventType(KtxEvent.EventType.UNUSUAL_BEHAVIOR)
                    .level(KtxEvent.Level.WARNING)
                    .criticality(KtxEvent.Criticality.REGULAR)
                    .userId(event.getUserId())
                    .ipAddress(event.getIpAddress())
                    .message("Suspicious login success after failure")
                    .source("security-service");

            logEvent(eventBuilder.build());
        }

        long recentUnusualBehaviorCount = events.stream()
                .filter(e -> e.eventType() == KtxEvent.EventType.UNUSUAL_BEHAVIOR)
                .filter(e -> e.getTimestamp() != null &&
                        e.getTimestamp().isAfter(LocalDateTime.now().minusDays(usualBehaviourProperties.getDays())))
                .count();

        if (recentUnusualBehaviorCount >= usualBehaviourProperties.getCount()) {
            LOGGER.warn("User {} has {} unusual behaviors in last {} days",
                    event.getUserId(), recentUnusualBehaviorCount, usualBehaviourProperties.getDays());

            eventBuilder.
                    eventType(KtxEvent.EventType.SECURITY).
                    level(KtxEvent.Level.ERROR).
                    criticality(KtxEvent.Criticality.CRITICAL).
                    userId(event.getUserId()).
                    ipAddress(event.getIpAddress()).
                    message("Repeated unusual behavior detected").
                    source("security-service");
            logEvent(eventBuilder.build());
        }
    }

    /**
     * Persists the provided event and sends it to the event bus.
     *
     * @param event the event to log
     * @return the saved event
     */
    public SystemEvent logEvent(SystemEvent event) {
        SystemEvent savedEvent = null;
        try {
            savedEvent = eventStore.save(event);
            producer.sendEvent(savedEvent);
        } catch (Exception e) {
            LOGGER.error("Failed to log event: {}", e.getMessage());
            return null;
        }
        return savedEvent;
    }

    public List<SystemEvent> findByType(KtxEvent.EventType eventType) {
        return eventStore.findByType(eventType);
    }

    public List<SystemEvent> findAllByOrder(Sort.Direction direction) {
        return eventStore.findAllByOrder(direction);
    }

    public SystemEvent findById(String id) {
        return eventStore.findById(id);
    }

    public List<SystemEvent> findByUserId(String id) {
        return eventStore.findByUserId(id);
    }
}