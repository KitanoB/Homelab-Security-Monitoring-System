package ktx.kitano.audit.service.infrastructure.messaging;

import com.kitano.iface.KtxEventProducer;
import ktx.kitano.audit.service.domain.SecurityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer implementation for SecurityEvent.
 */
@Service
public class SecurityEventProducer implements KtxEventProducer<SecurityEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventProducer.class);
    private static final String TOPIC = "security-events";

    private final KafkaTemplate<String, SecurityEvent> kafkaTemplate;

    public SecurityEventProducer(KafkaTemplate<String, SecurityEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, SecurityEvent event) {
        LOGGER.debug("Sending event to topic '{}': {}", topicName, event);

        CompletableFuture<SendResult<String, SecurityEvent>> future = kafkaTemplate.send(topicName, event);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                LOGGER.error("Kafka Error while sending event to topic '{}': {}", topicName, exception.getMessage(), exception);
            } else {
                LOGGER.info("Event successfully sent to topic '{}'", topicName);
            }
        });
    }
}