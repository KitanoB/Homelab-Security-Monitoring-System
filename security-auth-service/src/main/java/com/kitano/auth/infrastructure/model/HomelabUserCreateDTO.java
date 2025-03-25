package com.kitano.auth.infrastructure.model;


import lombok.Data;

@Data
public class HomelabUserCreateDTO {

    private String username;
    private String email;
    private String password;
}
