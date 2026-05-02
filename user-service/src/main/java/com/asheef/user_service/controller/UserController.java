package com.asheef.user_service.controller;

import com.asheef.user_service.entity.User;
import com.asheef.user_service.model.dto.UpdateUserDto;
import com.asheef.user_service.model.dto.UserRequestDto;
import com.asheef.user_service.model.dto.UsersDto;
import com.asheef.user_service.service.UserService;
import com.asheef.user_service.util.ResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }


    @PostMapping
    public ResponseEntity<ResponseDto> createUser(@Valid @RequestBody UserRequestDto dto) {
        return userService.createUser(dto);
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getUsers(@ModelAttribute UsersDto dto) {
        return userService.getUsers(dto);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ResponseDto> changeUserStatus(@PathVariable Integer userId, @RequestParam Boolean isActive) {
        return userService.changeUserStatus(userId, isActive);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable Integer id, @Valid @RequestBody UpdateUserDto dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable Integer id) {
        return userService.deleteUser(id);
    }
}
