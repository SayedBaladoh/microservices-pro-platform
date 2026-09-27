package com.microservices.pro.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * SecurityConfig — Session 20, Lab 16.
 * Implement the TODOs. See docs/labs/session-20-lab-16.md.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // TODO 1: permitAll() for /actuator/health and GET /api/v1/products/**
    // TODO 2: hasRole("ADMIN") for /api/admin/**
    // TODO 3: anyExchange().authenticated()
    // TODO 4: oauth2ResourceServer with keycloakJwtConverter()
    //         that maps realm_access.roles → ROLE_* GrantedAuthorities
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        throw new UnsupportedOperationException("TODO: implement SecurityConfig");
    }
}
