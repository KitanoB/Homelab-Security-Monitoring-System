package ktx.kitano.audit.service.infrastructure.repository;

import com.kitano.iface.model.KtxEvent;
import ktx.kitano.audit.service.domain.SecurityEvent;
import ktx.kitano.audit.service.domain.SecurityEventException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SecurityEventJpaStore implements SecurityEventStore<SecurityEvent> {

    private final SecurityEventJpaRepository jpaRepository;

    public SecurityEventJpaStore(SecurityEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SecurityEvent save(SecurityEvent event) throws Exception {
        if (event == null) {
            throw new SecurityEventException("Event cannot be null");
        }
        SecurityEvent savedEvent = jpaRepository.save(event);
        return savedEvent;
    }


    @Override
    public List<SecurityEvent> findByType(KtxEvent.EventType eventType) {
        return jpaRepository.findByEventType(eventType);
    }

    @Override
    public List<SecurityEvent> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public SecurityEvent findById(String id) {
        return jpaRepository.findById(id).orElse(null);
    }

    @Override
    public List<SecurityEvent> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId);
    }
}