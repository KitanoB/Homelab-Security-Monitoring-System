package ktx.kitano.audit.service.infrastructure;

import ktx.kitano.audit.service.domain.SecurityEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SecurityEventProducer implements ISecurityEventProducer {

    private static final String TOPIC = "security-events";
    private final KafkaTemplate<String, SecurityEvent> kafkaTemplate;

    public SecurityEventProducer(KafkaTemplate<String, SecurityEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, SecurityEvent event) {
        CompletableFuture<SendResult<String, SecurityEvent>> future = kafkaTemplate.send(topicName, event);

        // ✅ Use proper async handling
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                System.err.println("Kafka Error: " + exception.getMessage());
            } else {
                System.out.println("Event sent successfully to topic: " + topicName);
            }
        });
    }
}