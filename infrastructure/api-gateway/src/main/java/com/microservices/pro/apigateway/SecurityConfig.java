package com.microservices.pro.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SecurityConfig — Session 20, Lab 16.
 *
 * Replaces Session 3's JwtAuthFilter + JwtUtil entirely.
 *
 * Key changes:
 *   - NO shared secret — validation uses Keycloak's JWKS (public keys)
 *   - spring.security.oauth2.resourceserver.jwt.issuer-uri in application.yml
 *     tells Spring where to fetch the JWKS and validate 'iss' claim
 *   - realm_access.roles mapped to Spring Security GrantedAuthorities
 *   - Public routes remain unauthenticated (products GET, actuator/health)
 *   - ADMIN role required for /api/admin/** routes
 *
 * Session 3 files DELETED (not deprecated, not commented out):
 *   JwtAuthFilter.java  — Spring Security replaces its role entirely
 *   JwtUtil.java        — JWKS-based validation needs no manual parsing
 *   JwtConfig.java      — jwt.secret key removed from application.yml
 *
 * DEV ONLY — CSRF disabled for REST APIs; re-enable for browser form-based apps.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public routes — no token required
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("GET", "/api/v1/products/**").permitAll()
                        // Admin-only route — requires ROLE_ADMIN from Keycloak realm_access
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        // All other routes require a valid Keycloak JWT
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter()))
                )
                .build();
    }

    /**
     * Maps Keycloak's realm_access.roles claim into Spring Security
     * GrantedAuthorities (prefixed with ROLE_ to work with hasRole()).
     *
     * Without this converter, Spring Security reads only the 'scope' claim
     * and misses Keycloak's realm roles entirely — all role-protected routes
     * would permanently return 403. See Common Issues §3 (Session 20 docx).
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> keycloakJwtConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess =
                    (Map<String, Object>) jwt.getClaim("realm_access");
            if (realmAccess == null) return List.of();

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) return List.of();

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
