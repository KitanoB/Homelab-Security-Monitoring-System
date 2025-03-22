package ktx.kitano.security.service.infrastructure.repository;

import com.kitano.core.model.SystemEvent;
import com.kitano.iface.model.KtxEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventJpaRepository extends JpaRepository<SystemEvent, String> {
    List<SystemEvent> findByEventType(KtxEvent.EventType eventType);

    List<SystemEvent> findByUserId(String userId);

    List<SystemEvent> findAll(Sort sort);
}