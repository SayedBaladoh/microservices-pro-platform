package com.microservices.pro.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JwtUtil — Session 3, "Custom Filters & JWT Validation".
 *
 * Validates a JWT's signature locally using the shared secret — no call to
 * an authentication server on every request (stateless validation). See
 * Session 3 docx, 3.1 Cold Call Q3 and 3.3 Step 2.
 *
 * SECRET: must match the value used by tools/jwt-generator to sign test
 * tokens, or every signature check below will fail with no useful error
 * for trainees. Both modules first look at the JWT_SECRET environment
 * variable; if it isn't set, both fall back to the SAME hardcoded
 * dev-placeholder string. See tools/jwt-generator/.../JwtGeneratorApplication.java
 * for the matching value.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();  // throws if invalid, tampered, or expired
    }

    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
