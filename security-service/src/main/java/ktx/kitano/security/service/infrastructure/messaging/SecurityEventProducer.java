package ktx.kitano.security.service.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class SecurityEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventProducer.class);

    private final KafkaOperations<String, SystemEvent> kafka;

    public SecurityEventProducer(KafkaOperations<String, SystemEvent> kafka) {
        this.kafka = kafka;
    }

    public boolean sendEvent(SystemEvent event) {
        Message<SystemEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("kafka_topic", "auth-events")
                .build();

        CompletableFuture<SendResult<String, SystemEvent>> future = kafka.send(message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                RecordMetadata meta = result.getRecordMetadata();
                LOGGER.info("Event sent to topic {} partition {} offset {}",
                        meta.topic(), meta.partition(), meta.offset());
            } else {
                LOGGER.error("Failed to send event: {}", ex.getMessage(), ex);
            }
        });
        return true;
    }
}