package com.kitano.auth.model;


import lombok.Data;

@Data
public class HomelabUserCreateDTO {

    private String username;
    private String email;
    private String password;
}
