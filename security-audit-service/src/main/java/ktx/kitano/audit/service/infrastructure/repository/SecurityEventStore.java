package ktx.kitano.audit.service.infrastructure.repository;

import com.kitano.iface.model.KtxEvent;
import com.kitano.iface.KtxEventService;

import java.util.List;

public interface SecurityEventStore<T extends KtxEvent<?>> extends KtxEventService<T> {
    List<T> findByType(KtxEvent.EventType eventType);
    List<T> findByUserId(String userId);
}