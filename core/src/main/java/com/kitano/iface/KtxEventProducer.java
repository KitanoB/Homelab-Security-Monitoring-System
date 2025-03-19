package com.kitano.iface;

/**
 * Generic interface for producing events and sending them to a messaging system.
 *
 * @param <T> Type of event, must extend KtxEvent<?>
 */
public interface KtxEventProducer<T extends KtxEvent<?>> {

    /**
     * Sends an event to the specified topic.
     *
     * @param topic The topic where the event should be sent.
     * @param event The event to send.
     */
    void send(String topic, T event);
}