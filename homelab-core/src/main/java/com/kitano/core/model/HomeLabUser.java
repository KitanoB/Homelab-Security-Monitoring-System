package com.kitano.core.model;

import com.kitano.iface.model.KtxRole;
import com.kitano.iface.model.KtxUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
public class HomeLabUser implements KtxUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // ou AUTO, si tu préfères
    private String id;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('USER','MANAGER','ADMIN')", nullable = false)
    private KtxRole role;

    @Column(nullable = false)
    private LocalDateTime created;

    @Column(nullable = false, name = "enabled")
    private boolean enabled = true;


    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.created == null) {
            this.created = LocalDateTime.now();
        }
        if (this.version == null) {
            this.version = 0L;
        }
    }

    @Override
    public boolean isBan() {
        return !enabled;
    }

    @Override
    public void setBan(boolean ban) {
        this.enabled = !ban;
    }
}