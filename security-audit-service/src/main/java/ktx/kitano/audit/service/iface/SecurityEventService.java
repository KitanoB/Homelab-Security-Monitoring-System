package ktx.kitano.audit.service.iface;

import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;

import java.util.List;
import java.util.UUID;

public interface SecurityEventService {
    SecurityEvent save(SecurityEvent event) throws SecurityEventException;

    List<SecurityEvent> findByType(String eventType);

    List<SecurityEvent> findAll();

    SecurityEvent findById(UUID id);
}