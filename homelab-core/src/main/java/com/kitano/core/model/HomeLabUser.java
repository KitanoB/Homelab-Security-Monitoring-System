package com.kitano.core.model;

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

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime created;


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
}