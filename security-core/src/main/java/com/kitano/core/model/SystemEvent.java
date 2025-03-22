package com.kitano.core.model;

import com.kitano.iface.model.KtxEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_events")
public class SystemEvent implements KtxEvent<String> {

    @Id
    @Column(columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private String id;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Criticality criticality;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "source", nullable = false)
    private String source;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        if (this.timestamp == null) this.timestamp = LocalDateTime.now();
        if (this.version == null) this.version = 0L;
    }

    // KtxEvent interface implementation
    @Override
    public Level level() {
        return level;
    }

    @Override
    public Criticality criticality() {
        return criticality;
    }

    @Override
    public EventType eventType() {
        return eventType;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getPayload() {
        return userId;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String toJson() {
        return KtxEvent.super.toJson();
    }

    @Override
    public String toString() {
        return "SystemEvent{" +
                "id=" + id +
                ", version=" + version +
                ", timestamp=" + timestamp +
                ", eventType=" + eventType +
                ", level=" + level +
                ", criticality=" + criticality +
                ", userId='" + userId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", message='" + message + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}