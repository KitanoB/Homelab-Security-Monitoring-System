package ktx.kitano.security.service.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import ktx.kitano.security.service.application.SecurityService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventConsumer {
    private final SecurityService service;

    public SecurityEventConsumer(SecurityService service) {
        this.service = service;
    }

    @KafkaListener(topics = "auth-events", groupId = "security-service")
    public void consume(ConsumerRecord<String, SystemEvent> systemEventConsumerRecord) {
        SystemEvent event = systemEventConsumerRecord.value();
        service.secure(event);
    }
}