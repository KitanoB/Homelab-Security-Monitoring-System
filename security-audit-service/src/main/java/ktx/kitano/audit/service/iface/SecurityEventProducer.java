package ktx.kitano.audit.service.iface;

import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;

public interface SecurityEventProducer {
    void sendEvent(SecurityEvent event) throws SecurityEventException;
}
