package com.kitano.auth.model;


public record UserLoginDTO(
        String username,
        String password
) {
}