package com.asheef.user_service.service;

import com.asheef.user_service.model.dto.UpdateUserDto;
import com.asheef.user_service.model.dto.UserRequestDto;
import com.asheef.user_service.model.dto.UsersDto;
import com.asheef.user_service.util.ResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<ResponseDto> createUser(UserRequestDto dto);

    ResponseEntity<ResponseDto> getUsers(UsersDto dto);

    ResponseEntity<ResponseDto> changeUserStatus(Integer userId, Boolean isActive);

    ResponseEntity<ResponseDto> updateUser(Integer id,UpdateUserDto dto);

    ResponseEntity<ResponseDto> deleteUser(Integer id);
}
