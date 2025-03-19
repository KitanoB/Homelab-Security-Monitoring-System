package ktx.kitano.audit.service.application;

import com.kitano.iface.KtxEvent;
import com.kitano.iface.KtxEventService;
import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;
import ktx.kitano.audit.service.infrastructure.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


/**
 * Service class for managing Security Events.
 */
@Service
public class SecurityService implements KtxEventService<SecurityEvent> {

    private final SecurityEventRepository<SecurityEvent> repository;

    public SecurityService(SecurityEventRepository<SecurityEvent> repository) {
        this.repository = repository;
    }

    @Override
    public SecurityEvent save(SecurityEvent event) throws Exception {
        if (event == null) {
            throw new SecurityEventException("Event cannot be null");
        }
        return repository.save(event);
    }

    @Override
    public List<SecurityEvent> findByType(KtxEvent.EventType eventType) {
        return Collections.unmodifiableList(repository.findByType(eventType));
    }

    @Override
    public List<SecurityEvent> findAll() {
        return Collections.unmodifiableList(repository.findAll());
    }

    @Override
    public SecurityEvent findById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<SecurityEvent> findByUserId(String id) {
        return Collections.unmodifiableList(repository.findByUserId(id));
    }
}