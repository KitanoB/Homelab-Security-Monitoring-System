package ktx.kitano.audit.service.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Version
    @Column(nullable = false)
    private Long version = 0L;  // ✅ Ensures version is never null

    private LocalDateTime timestamp;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "username")
    private String username;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "message")
    private String message;

    public SecurityEvent() {
    }

    public SecurityEvent(String eventType, String username, String ipAddress, String message) {
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
        this.username = username;
        this.ipAddress = ipAddress;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}