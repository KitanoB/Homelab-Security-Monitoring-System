package com.kitano.auth.model;

public record UserTokenResponseDTO(
        String token,
        UserResponseDTO user
) {
}