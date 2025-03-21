package com.kitano.iface.model;

import java.time.LocalDateTime;

public interface KtxUser {
    String getId();

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    String getRole();

    void setRole(String role);

    LocalDateTime getCreated();
}
