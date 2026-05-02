package com.asheef.auth_service.controller;

import com.asheef.auth_service.model.dto.LoginRequest;
import com.asheef.auth_service.service.AuthService;
import com.asheef.user_service.util.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
