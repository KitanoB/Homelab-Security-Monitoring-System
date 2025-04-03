package com.kitano.iface.model;

import java.time.LocalDateTime;

public interface KtxUser {
    String getId();

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    Enum getRole();

    void setRole(KtxRole role);

    LocalDateTime getCreated();

    boolean isBan();

    void setBan(boolean ban);
}
