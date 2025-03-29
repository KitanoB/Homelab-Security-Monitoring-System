package com.kitano.auth.model;

import com.kitano.core.model.HomeLabUser;

public class UserMapper {

    public static HomelabUserDTO toDto(HomeLabUser user) {
        return new HomelabUserDTO(
                user.getId(),
                user.getUsername()
        );
    }

    public static HomeLabUser fromCreateDto(HomelabUserCreateDTO dto, String hashedPassword) {
        HomeLabUser user = new HomeLabUser();
        user.setUsername(dto.getUsername());
        user.setPassword(hashedPassword);
        return user;
    }
}