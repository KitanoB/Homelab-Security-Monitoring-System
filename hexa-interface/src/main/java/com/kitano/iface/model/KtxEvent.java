package com.kitano.iface.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

public interface KtxEvent<T> {

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

    public enum EventType {
        AUTH,
        USER_ACTION,
        SYSTEM,
        APPLICATION,
        SECURITY,
        LOGIN_ATTEMPT
    }
}