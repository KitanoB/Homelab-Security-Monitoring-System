package com.kitano.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginDTO {
    String username;
    String password;
    String ipAddress;
}