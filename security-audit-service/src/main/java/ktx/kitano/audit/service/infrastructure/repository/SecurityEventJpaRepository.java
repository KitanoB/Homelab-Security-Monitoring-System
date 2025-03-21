package ktx.kitano.audit.service.infrastructure.repository;

import com.kitano.iface.model.KtxEvent;
import ktx.kitano.audit.service.domain.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventJpaRepository extends JpaRepository<SecurityEvent, String> {
    List<SecurityEvent> findByEventType(KtxEvent.EventType eventType);
    List<SecurityEvent> findByUserId(String userId);
}