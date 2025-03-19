package com.kitano.iface;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

public interface KtxEvent<T> {

    String getEventId();

    Level level();

    Criticality criticality();

    EventType eventType();

    LocalDateTime getTimestamp();

    T getPayload();

    String getSource();

    default String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    enum Level {
        INFO, WARNING, ERROR
    }

    enum Criticality {
        LOW, REGULAR, CRITICAL
    }

    enum EventType {
        AUTH, SECURITY, SYSTEM, APPLICATION, USER_ACTION
    }
}