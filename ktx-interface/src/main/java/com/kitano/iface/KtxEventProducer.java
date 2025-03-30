package com.kitano.iface;

import com.kitano.iface.model.KtxEvent;

/**
 * Generic interface for producing events and sending them to a messaging system.
 *
 * @param <T> Type of event, must extend KtxEvent<?>
 */
public interface KtxEventProducer<T extends KtxEvent<?>> {
    void sendEvent(T event);
}