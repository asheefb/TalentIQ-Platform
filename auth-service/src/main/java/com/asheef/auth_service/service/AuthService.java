package com.asheef.auth_service.service;

import com.asheef.auth_service.model.dto.LoginRequest;
import com.asheef.user_service.util.ResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ResponseDto> login(LoginRequest loginRequest);
}
