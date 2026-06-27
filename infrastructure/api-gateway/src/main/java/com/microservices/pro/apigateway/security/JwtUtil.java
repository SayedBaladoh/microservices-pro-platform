package com.microservices.pro.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtUtil — Session 3, "Custom Filters & JWT Validation".
 *
 * Implement the TODOs below. See docs/labs/session-03-lab-2b.md for the
 * full lab instructions and docs/labs/session-03-jwt-testing.md for how to
 * generate test tokens with tools/jwt-generator.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // TODO 1: Implement validateToken(String token) → returns Claims
    //         Use Jwts.parserBuilder() with an HMAC-SHA key
    //         (Keys.hmacShaKeyFor(secret.getBytes(...)))
    //         Throws JwtException if invalid or expired
    public Claims validateToken(String token) {
        throw new UnsupportedOperationException("TODO: implement validateToken()");
    }

    // TODO 2: Implement isTokenValid(String token) → returns boolean
    //         Wrap validateToken() in a try-catch on JwtException
    public boolean isTokenValid(String token) {
        throw new UnsupportedOperationException("TODO: implement isTokenValid()");
    }
}
