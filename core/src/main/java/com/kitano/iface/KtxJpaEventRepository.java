package com.kitano.iface;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Generic JPA repository for KtxEvent-based entities.
 *
 * @param <T> Type of event, must extend KtxEvent<?>
 */
@Repository
public interface KtxJpaEventRepository<T extends KtxEvent<?>> extends JpaRepository<T, String> {

    /**
     * Finds events by their type.
     *
     * @param eventType The event type to filter.
     * @return List of matching events.
     */
    List<T> findByEventType(KtxEvent.EventType eventType);

    /**
     * Finds events by their user ID.
     *
     * @param userId The user ID to filter.
     * @return List of matching events.
     */
    List<T> findByUserId(String userId);
}