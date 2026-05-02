package com.asheef.auth_service.service;

import com.asheef.auth_service.model.dto.LoginRequest;
import com.asheef.auth_service.model.dto.RegisterRequest;
import com.asheef.auth_service.model.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest registerRequest);

}
