package com.asheef.user_service.mapper;

import com.asheef.user_service.entity.User;
import com.asheef.user_service.model.response.UserResponseDto;

public class UserMapperOld {

    public static UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobile(),
                user.getAddress(),
                user.getIsActive()
        );
    }
}
