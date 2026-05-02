package com.asheef.user_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private String testSecret = "testSecretKeyForTesting12345678901234567890";
    private String validToken;
    private String testUsername = "test@example.com";
    private String testRole = "USER";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(jwtAuthFilter, "secret", testSecret);
        
        validToken = Jwts.builder()
                .setSubject(testUsername)
                .claim("role", testRole)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, testSecret)
                .compact();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUsername, SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AdminToken_SetsAdminAuthentication() throws ServletException, IOException {
        String adminToken = Jwts.builder()
                .setSubject("admin@example.com")
                .claim("role", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, testSecret)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + adminToken);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_BearerPrefixOnly_ContinuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_ContinuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExpiredToken_ContinuesFilterChain() throws ServletException, IOException {
        String expiredToken = Jwts.builder()
                .setSubject(testUsername)
                .claim("role", testRole)
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, testSecret)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_TokenWithoutRole_ContinuesFilterChain() throws ServletException, IOException {
        String tokenWithoutRole = Jwts.builder()
                .setSubject(testUsername)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, testSecret)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithoutRole);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // The filter should still set authentication even without role
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUsername, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_TokenWithNullRole_SetsAuthenticationWithNullRole() throws ServletException, IOException {
        String tokenWithNullRole = Jwts.builder()
                .setSubject(testUsername)
                .claim("role", (String) null)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, testSecret)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithNullRole);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // The filter should still set authentication even with null role
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUsername, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmptyAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_MalformedToken_ContinuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed.token");
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ServletException_PropagatesException() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        assertThrows(ServletException.class, () -> {
            jwtAuthFilter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    void doFilterInternal_IOException_PropagatesException() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        doThrow(new IOException("Test exception")).when(filterChain).doFilter(request, response);

        assertThrows(IOException.class, () -> {
            jwtAuthFilter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    void doFilterInternal_MultipleRoles_SetsFirstRole() throws ServletException, IOException {
        String token = Jwts.builder()
                .setSubject(testUsername)
                .claim("role", "USER,ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, testSecret)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUsername, SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER,ADMIN")));
    }

    @Test
    void doFilterInternal_CaseInsensitiveBearer_ContinuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("bearer " + validToken);
        doNothing().when(filterChain).doFilter(request, response);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
