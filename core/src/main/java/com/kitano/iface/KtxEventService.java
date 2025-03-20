package com.kitano.iface;

import java.util.List;

/**
 * Generic service interface for handling KtxEvent-based entities.
 *
 * @param <T> Type of event, must extend KtxEvent<?>
 */
public interface KtxEventService<T extends KtxEvent<?>> {

    /**
     * Saves an event in the database.
     *
     * @param event The event to save.
     * @return The saved event.
     * @throws Exception If an error occurs during saving.
     */
    T save(T event) throws Exception;

    /**
     * Retrieves events by their type.
     *
     * @param eventType The type of event to filter.
     * @return List of events matching the type.
     */
    List<T> findByType(KtxEvent.EventType eventType);

    /**
     * Retrieves all events from the database.
     *
     * @return List of all events.
     */
    List<T> findAll();

    /**
     * Retrieves a specific event by its unique ID.
     *
     * @param id String of the event.
     * @return The found event or null if not found.
     */
    T findById(String id);

    /**
     * Retrieves a specific event by its unique ID.
     *
     * @param id String of the event.
     * @return The found event or null if not found.
     */
    List<T> findByUserId(String id);

}