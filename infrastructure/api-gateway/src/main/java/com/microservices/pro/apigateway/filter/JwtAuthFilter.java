package com.microservices.pro.apigateway.filter;

import com.microservices.pro.apigateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JwtAuthFilter — Session 3, "Custom Filters & JWT Validation".
 *
 * Implement the TODOs below. See docs/labs/session-03-lab-2b.md for the
 * full lab instructions and acceptance criteria.
 */
@org.springframework.stereotype.Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();    
    private record PublicRoute(HttpMethod method, String pathPattern) {}

    // TODO 1: Define PUBLIC_ROUTES list (at minimum: GET "/api/v1/products/**" — GET all and get by Id is public)
    private static final List<PublicRoute> PUBLIC_ROUTES = List.of();

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO 2: Implement filter() method:
        //   - Skip validation for public routes
        //   - Extract the Authorization header, expect "Bearer <token>"
        //   - Return 401 if missing or invalid
        //   - Validate the JWT via jwtUtil
        //   - Add X-User-Id and X-User-Role headers from the JWT claims
        //   - Forward the enriched request to the chain
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // TODO 3: Return Ordered.HIGHEST_PRECEDENCE + 1
        //         (must run AFTER LoggingFilter, which is HIGHEST_PRECEDENCE)
        return 0;
    }
}
