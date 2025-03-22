package ktx.kitano.security.service.application;

import com.kitano.core.model.SystemEvent;
import com.kitano.iface.model.KtxEvent;
import com.kitano.core.model.SystemException;
import ktx.kitano.security.service.infrastructure.messaging.SecurityEventProducer;
import ktx.kitano.security.service.infrastructure.repository.SecurityEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Service class for managing Security Events.
 */
@Service
public class SecurityService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    private final SecurityEventStore<SystemEvent> eventStore;
    private final SecurityEventProducer producer;

    @Autowired
    public SecurityService(SecurityEventStore<SystemEvent> eventStore, SecurityEventProducer producer) {
        this.eventStore = eventStore;
        this.producer = producer;
    }

    public SystemEvent logEvent(SystemEvent event) throws Exception {
        if (event == null) {
            throw new SystemException("Event cannot be null");
        }
        SystemEvent savedEvent = eventStore.save(event);
        producer.sendEvent(savedEvent);
        return savedEvent;
    }

    public List<SystemEvent> findByType(KtxEvent.EventType eventType) {
        return eventStore.findByType(eventType);
    }

    public List<SystemEvent> findAll() {
        return eventStore.findAll();
    }

    public SystemEvent findById(String id) {
        return eventStore.findById(id);
    }

    public List<SystemEvent> findByUserId(String id) {
        return eventStore.findByUserId(id);
    }
}