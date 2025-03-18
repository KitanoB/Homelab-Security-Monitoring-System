package ktx.kitano.audit.service.infrastructure;

import ktx.kitano.audit.service.domain.SecurityEvent;

public interface ISecurityEventProducer {
    void send(String topicName, SecurityEvent event);
}