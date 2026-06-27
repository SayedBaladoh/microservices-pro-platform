package com.microservices.pro.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtilTest — Session 3, Lab 2B, Task 3.
 *
 * Matches the original lab spec's 3 required tests exactly:
 *   Test 1: isTokenValid() returns true for a valid token
 *   Test 2: isTokenValid() returns false for an expired token
 *   Test 3: isTokenValid() returns false for a tampered token
 *
 * Uses a fixed test-only secret injected via ReflectionTestUtils — this is
 * NOT the same secret as application.yml's jwt.secret, and that's fine:
 * JwtUtil only needs *some* secret to sign/verify against in this unit
 * test; it doesn't depend on the real running Gateway's configuration.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final String TEST_SECRET =
            "test-only-secret-key-for-jwtutil-unit-tests-not-for-real-use";

    @InjectMocks
    private JwtUtil jwtUtil;

    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void isTokenValid_returnsTrue_forAValidToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000)) // 1 hour
                .signWith(signingKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forAnExpiredToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() - 1_000)) // already in the past
                .signWith(signingKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forATamperedToken() {
        String token = Jwts.builder()
                .setSubject("user1")
                .claim("role", "CUSTOMER")
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(signingKey)
                .compact();

        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a"); // flip the last signature character

        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }
}
