package ktx.kitano.security.service.infrastructure.repository;

import com.kitano.iface.KtxEventService;
import com.kitano.iface.model.KtxEvent;

import java.util.List;

public interface SecurityEventStore<T extends KtxEvent<?>> extends KtxEventService<T> {
    List<T> findByType(KtxEvent.EventType eventType);

    List<T> findByUserId(String userId);

    List<T> findByUserId(String userId, int limit);
}