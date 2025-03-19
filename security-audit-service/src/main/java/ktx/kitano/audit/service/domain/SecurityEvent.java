package ktx.kitano.audit.service.domain;

import com.kitano.iface.KtxEvent;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "security_events")
public class SecurityEvent implements KtxEvent<String> {

    @Id
    @Column(columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private String id;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
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

    public SecurityEvent() {
    }

    public SecurityEvent(EventType eventType, Level level, Criticality criticality,
                         String userId, String ipAddress, String message) {
        this.eventType = eventType;
        this.level = level;
        this.criticality = criticality;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @Override
    public String getEventId() {
        return id;
    }

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
        return "Security Service";
    }

    @Override
    public String toJson() {
        return KtxEvent.super.toJson();
    }

    @Override
    public String toString() {
        return "SecurityEvent{" +
                "id=" + id +
                ", version=" + version +
                ", timestamp=" + timestamp +
                ", eventType=" + eventType +
                ", level=" + level +
                ", criticality=" + criticality +
                ", userId='" + userId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}