package ktx.kitano.security.service.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SecurityEventProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventProducer.class);
    private static final String TOPIC = "security-events";

    private final KafkaTemplate<String, SystemEvent> kafkaTemplate;

    public SecurityEventProducer(KafkaTemplate<String, SystemEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(SystemEvent event) {
        LOGGER.debug("Sending event to topic '{}': {}", TOPIC, event);

        CompletableFuture<SendResult<String, SystemEvent>> future = kafkaTemplate.send(TOPIC, event);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                LOGGER.error("Kafka Error while sending event to topic '{}': {}", TOPIC, exception.getMessage(), exception);
            } else {
                LOGGER.info("Event successfully sent to topic '{}'", TOPIC);
            }
        });
    }
}