package com.asheef.auth_service.service.impl;


import com.asheef.auth_service.client.UserServiceClient;
import com.asheef.auth_service.config.JwtUtil;
import com.asheef.auth_service.model.dto.RegisterRequest;
import com.asheef.auth_service.model.response.LoginResponse;
import com.asheef.auth_service.model.response.UserCredentialDto;
import com.asheef.auth_service.service.AuthService;

import com.asheef.auth_service.model.dto.LoginRequest;

import com.asheef.auth_service.constants.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserServiceClient userServiceClient,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userServiceClient = userServiceClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        UserCredentialDto user;
        try {
            user = userServiceClient.findByEmail(request.getEmail());
        } catch (java.util.NoSuchElementException e) {
            // Uniform error to prevent user-enumeration.
            log.warn("Login failed (unknown user) email={}", request.getEmail());
            throw new IllegalArgumentException(Constant.INVALID_CREDENTIALS);
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            log.warn("Login denied for deactivated account email={}", request.getEmail());
            throw new IllegalStateException(Constant.ACCOUNT_DEACTIVATED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed (bad password) email={}", request.getEmail());
            throw new IllegalArgumentException(Constant.INVALID_CREDENTIALS);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        log.info("Login success email={} role={}", user.getEmail(), user.getRole());

        return new LoginResponse(
                token,
                "Bearer",
                jwtUtil.getExpirationSeconds(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public void register(RegisterRequest request) {
        log.info("Register request email={}", request.getEmail());
        userServiceClient.register(request);
    }

}
