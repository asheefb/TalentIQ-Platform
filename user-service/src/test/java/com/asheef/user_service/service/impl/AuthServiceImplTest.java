package com.asheef.user_service.service.impl;

import com.asheef.user_service.config.JwtUtil;
import com.asheef.user_service.constants.Constant;
import com.asheef.user_service.entity.User;
import com.asheef.user_service.enums.Role;
import com.asheef.user_service.repository.UserRepository;
import com.asheef.user_service.util.ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setMobile("9876543210");
        testUser.setAddress("Test Address");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setRole(Role.USER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(validToken);

        ResponseEntity<ResponseDto> response = authService.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(200, response.getBody().getStatus());
        assertEquals(validToken, response.getBody().getData());
        assertEquals(Constant.LOGIN_SUCCESS, response.getBody().getMessage());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil).generateToken("test@example.com", "USER");
    }

    @Test
    void login_AdminUser_Success() {
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(validToken);

        ResponseEntity<ResponseDto> response = authService.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(validToken, response.getBody().getData());

        verify(jwtUtil).generateToken("test@example.com", "ADMIN");
    }

    @Test
    void login_UserNotFound_ThrowsNoSuchElementException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(Constant.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ThrowsIllegalArgumentException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(Constant.INVALID_PASSWORD, exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_InactiveUser_Success() {
        testUser.setIsActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(validToken);

        ResponseEntity<ResponseDto> response = authService.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(validToken, response.getBody().getData());
    }

    @Test
    void login_NullEmail_ThrowsException() {
        loginRequest.setEmail(null);

        assertThrows(Exception.class, () -> {
            authService.login(loginRequest);
        });

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_NullPassword_ThrowsException() {
        loginRequest.setPassword(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(Exception.class, () -> {
            authService.login(loginRequest);
        });

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_EmptyEmail_ThrowsNoSuchElementException() {
        loginRequest.setEmail("");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(Constant.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void login_EmptyPassword_ThrowsIllegalArgumentException() {
        loginRequest.setPassword("");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals(Constant.INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    void login_JwtTokenGeneration_ReturnsCorrectToken() {
        String expectedToken = "custom.jwt.token";
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(expectedToken);

        ResponseEntity<ResponseDto> response = authService.login(loginRequest);

        assertEquals(expectedToken, response.getBody().getData());
        verify(jwtUtil).generateToken("test@example.com", "USER");
    }

    @Test
    void login_DatabaseError_PropagatesException() {
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Database connection failed", exception.getMessage());
    }

    @Test
    void login_PasswordEncoderError_PropagatesException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenThrow(new RuntimeException("Password encoding error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Password encoding error", exception.getMessage());
    }

    @Test
    void login_JwtGenerationError_PropagatesException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenThrow(new RuntimeException("JWT generation error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("JWT generation error", exception.getMessage());
    }
}
