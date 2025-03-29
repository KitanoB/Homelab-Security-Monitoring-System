package com.kitano.auth.model;

public record UserRegisterDTO(
        String username,
        String email,
        String password
) {
}