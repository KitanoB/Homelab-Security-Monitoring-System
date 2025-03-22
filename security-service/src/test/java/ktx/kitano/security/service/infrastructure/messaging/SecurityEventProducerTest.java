package ktx.kitano.security.service.infrastructure.messaging;

import com.kitano.core.model.SystemEvent;
import com.kitano.iface.model.KtxEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.messaging.Message;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SecurityEventProducerTest {

    private KafkaOperations<String, SystemEvent> kafka;
    private SecurityEventProducer producer;

    @BeforeEach
    void setUp() {
        kafka = mock(KafkaOperations.class);
        producer = new SecurityEventProducer(kafka);
    }

    @Test
    void sendEvent_successfully() {
        // Arrange
        SystemEvent event = SystemEvent.builder()
                .eventType(KtxEvent.EventType.SYSTEM)
                .level(KtxEvent.Level.INFO)
                .criticality(KtxEvent.Criticality.REGULAR)
                .userId("user-123")
                .ipAddress("127.0.0.1")
                .message("Success")
                .source("security-service")
                .build();

        CompletableFuture<org.springframework.kafka.support.SendResult<String, SystemEvent>> future = new CompletableFuture<>();
        org.springframework.kafka.support.SendResult<String, SystemEvent> result = new org.springframework.kafka.support.SendResult<>(
                new ProducerRecord<>("auth-events", event),
                new RecordMetadata(null, 0, 0, 0, 0L, 0, 0)
        );
        future.complete(result);

        given(kafka.send(any(Message.class))).willReturn(future);

        // Act
        producer.sendEvent(event);

        // Assert
        verify(kafka).send(any(Message.class));
    }

    @Test
    void sendEvent_shouldLogErrorOnFailure() {
        // Arrange
        SystemEvent event = SystemEvent.builder()
                .eventType(KtxEvent.EventType.SYSTEM)
                .level(KtxEvent.Level.INFO)
                .criticality(KtxEvent.Criticality.REGULAR)
                .userId("user-123")
                .ipAddress("127.0.0.1")
                .message("Failure")
                .source("security-service")
                .build();

        CompletableFuture<org.springframework.kafka.support.SendResult<String, SystemEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka unavailable"));

        given(kafka.send(any(Message.class))).willReturn(future);

        // Act
        producer.sendEvent(event);

        // Assert
        verify(kafka).send(any(Message.class));
    }
}