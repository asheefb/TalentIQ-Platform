package com.asheef.auth_service.service.impl;


import com.asheef.auth_service.config.JwtUtil;
import com.asheef.auth_service.service.AuthService;

import com.asheef.auth_service.model.dto.LoginRequest;
import com.asheef.user_service.util.ResponseDto;
import com.asheef.auth_service.constants.Constant;
import com.asheef.user_service.entity.User;
import com.asheef.user_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<ResponseDto> login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NoSuchElementException(Constant.USER_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException(Constant.INVALID_PASSWORD);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(
                new ResponseDto(Boolean.TRUE, HttpStatus.OK.value(), token, Constant.LOGIN_SUCCESS)
        );
    }
}
