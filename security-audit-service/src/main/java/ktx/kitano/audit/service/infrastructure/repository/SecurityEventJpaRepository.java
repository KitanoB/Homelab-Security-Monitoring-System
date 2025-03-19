package ktx.kitano.audit.service.infrastructure.repository;

import com.kitano.iface.KtxJpaEventRepository;
import ktx.kitano.audit.service.domain.SecurityEvent;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventJpaRepository extends KtxJpaEventRepository<SecurityEvent> {
}