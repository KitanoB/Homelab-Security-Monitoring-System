package ktx.kitano.security.service.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import com.kitano.core.model.SystemException;
import ktx.kitano.security.service.application.SecurityService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventConsumer.class);
    private final SecurityService service;

    public SecurityEventConsumer(SecurityService service) {
        this.service = service;
    }

    @KafkaListener(topics = "auth-events", groupId = "security-service")
    public void consume(ConsumerRecord<String, SystemEvent> record) {
        SystemEvent event = record.value();
        LOGGER.info("Consumed SystemEvent from topic auth-events: {}", event);
        try {
            service.secure(event);
            service.logEvent(event);
        } catch (SystemException e) {
            LOGGER.error("Could not save event: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during event consumption", e);
        }
    }
}