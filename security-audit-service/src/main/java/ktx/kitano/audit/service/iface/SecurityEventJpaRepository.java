package ktx.kitano.audit.service.iface;

import ktx.kitano.audit.service.domain.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityEventJpaRepository extends JpaRepository<SecurityEvent, UUID> {
    List<SecurityEvent> findByEventType(String eventType);
}