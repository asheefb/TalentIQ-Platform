package com.asheef.user_service.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private String testSecret = "testSecretKeyForTesting1234567890123456789012345678901234567890";
    private String testUsername = "test@example.com";
    private String testRole = "USER";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_ValidInput_ReturnsValidToken() {
        String token = jwtUtil.generateToken(testUsername, testRole);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void generateToken_AdminRole_ReturnsValidToken() {
        String token = jwtUtil.generateToken(testUsername, "ADMIN");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void generateToken_ModeratorRole_ReturnsValidToken() {
        String token = jwtUtil.generateToken(testUsername, "MODERATOR");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void extractUsername_ValidToken_ReturnsCorrectUsername() {
        String token = jwtUtil.generateToken(testUsername, testRole);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void extractUsername_DifferentUsername_ReturnsCorrectUsername() {
        String differentUsername = "admin@example.com";
        String token = jwtUtil.generateToken(differentUsername, "ADMIN");

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(differentUsername, extractedUsername);
    }

    @Test
    void extractUsername_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void extractUsername_ExpiredToken_ReturnsUsername() {
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -1000L);

        String expiredToken = expiredJwtUtil.generateToken(testUsername, testRole);

        // Even expired tokens can be parsed to extract username
        String extractedUsername = jwtUtil.extractUsername(expiredToken);
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void generateToken_NullUsername_ReturnsToken() {
        String token = jwtUtil.generateToken(null, testRole);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(null, jwtUtil.extractUsername(token));
    }

    @Test
    void generateToken_EmptyUsername_ReturnsToken() {
        String token = jwtUtil.generateToken("", testRole);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_NullRole_ReturnsToken() {
        String token = jwtUtil.generateToken(testUsername, null);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(testUsername, jwtUtil.extractUsername(token));
    }

    @Test
    void generateToken_EmptyRole_ReturnsToken() {
        String token = jwtUtil.generateToken(testUsername, "");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_TokenContainsRoleClaim() {
        String token = jwtUtil.generateToken(testUsername, testRole);

        String role = Jwts.parser()
                .setSigningKey(testSecret)
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);

        assertEquals(testRole, role);
    }

    @Test
    void generateToken_TokenContainsCorrectSubject() {
        String token = jwtUtil.generateToken(testUsername, testRole);

        String subject = Jwts.parser()
                .setSigningKey(testSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        assertEquals(testUsername, subject);
    }

    @Test
    void generateToken_TokenContainsIssuedAt() {
        String token = jwtUtil.generateToken(testUsername, testRole);

        Date issuedAt = Jwts.parser()
                .setSigningKey(testSecret)
                .parseClaimsJws(token)
                .getBody()
                .getIssuedAt();

        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date(System.currentTimeMillis() + 1000)));
    }

    @Test
    void generateToken_TokenContainsExpiration() {
        String token = jwtUtil.generateToken(testUsername, testRole);

        Date expiration = Jwts.parser()
                .setSigningKey(testSecret)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void generateToken_DifferentSecrets_DifferentTokens() {
        JwtUtil differentJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentJwtUtil, "secret", "differentSecret1234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(differentJwtUtil, "expiration", 86400000L);

        String token1 = jwtUtil.generateToken(testUsername, testRole);
        String token2 = differentJwtUtil.generateToken(testUsername, testRole);

        assertNotEquals(token1, token2);
    }

    @Test
    void extractUsername_TokenWithWrongSecret_ThrowsException() {
        JwtUtil wrongJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(wrongJwtUtil, "secret", "wrongSecret1234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(wrongJwtUtil, "expiration", 86400000L);

        String token = wrongJwtUtil.generateToken(testUsername, testRole);

        assertThrows(io.jsonwebtoken.SignatureException.class, () -> {
            jwtUtil.extractUsername(token);
        });
    }

    @Test
    void generateToken_LongUsername_ReturnsValidToken() {
        String longUsername = "very.long.username.with.many.parts.for.testing.purposes@example.com";
        String token = jwtUtil.generateToken(longUsername, testRole);

        assertNotNull(token);
        assertEquals(longUsername, jwtUtil.extractUsername(token));
    }

    @Test
    void generateToken_SpecialCharactersInUsername_ReturnsValidToken() {
        String specialUsername = "test+user@example-domain.co.uk";
        String token = jwtUtil.generateToken(specialUsername, testRole);

        assertNotNull(token);
        assertEquals(specialUsername, jwtUtil.extractUsername(token));
    }
}
