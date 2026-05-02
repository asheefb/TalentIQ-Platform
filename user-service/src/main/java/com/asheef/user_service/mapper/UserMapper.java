package com.asheef.user_service.mapper;

import com.asheef.user_service.entity.User;
import com.asheef.user_service.model.response.UserResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toDto(User user);

    List<UserResponseDto> toDtoList(List<User> users);

}
