package ktx.kitano.audit.service.application;

import com.kitano.iface.model.KtxEvent;
import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;
import ktx.kitano.audit.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.audit.service.infrastructure.repository.SecurityEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Service class for managing Security Events.
 */
@Service
public class SecurityService {

    private final SecurityEventStore<SecurityEvent> eventStore;
    private final SecurityEventProducer producer;

    @Autowired
    public SecurityService(SecurityEventStore<SecurityEvent> eventStore, SecurityEventProducer producer) {
        this.eventStore = eventStore;
        this.producer = producer;
    }

    public SecurityEvent logEvent(SecurityEvent event) throws Exception {
        if (event == null) {
            throw new SecurityEventException("Event cannot be null");
        }
        SecurityEvent savedEvent = eventStore.save(event);
        producer.sendEvent(savedEvent);
        return savedEvent;
    }

    public List<SecurityEvent> findByType(KtxEvent.EventType eventType) {
        return eventStore.findByType(eventType);
    }

    public List<SecurityEvent> findAll() {
        return eventStore.findAll();
    }

    public SecurityEvent findById(String id) {
        return eventStore.findById(id);
    }

    public List<SecurityEvent> findByUserId(String id) {
        return eventStore.findByUserId(id);
    }
}