package com.asheef.auth_service.controller;

import com.asheef.auth_service.constants.Constant;
import com.asheef.auth_service.model.dto.LoginRequest;
import com.asheef.auth_service.model.dto.RegisterRequest;
import com.asheef.auth_service.model.response.LoginResponse;
import com.asheef.auth_service.service.AuthService;
import com.asheef.auth_service.util.ResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseDto> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), response, Constant.LOGIN_SUCCESS)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseDto(Boolean.TRUE, HttpStatus.CREATED.value(), Constant.USER_REGISTERED)
        );
    }
}
