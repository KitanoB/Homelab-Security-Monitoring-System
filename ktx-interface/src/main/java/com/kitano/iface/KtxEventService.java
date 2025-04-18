package com.kitano.iface;

import com.kitano.iface.model.KtxEvent;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Generic service interface for handling KtxEvent-based entities.
 *
 * @param <T> Type of event, must extend KtxEvent<?>
 */
public interface KtxEventService<T extends KtxEvent<?>> {
    T save(T event) throws Exception;

    List<T> findByType(KtxEvent.EventType eventType);

    List<T> findAllByOrder(Sort.Direction direction);

    T findById(String id);

    List<T> findByUserId(String userId);
}