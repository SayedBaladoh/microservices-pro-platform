package com.microservices.pro.apigateway.filter;

import com.microservices.pro.apigateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JwtAuthFilter — Session 3, "Custom Filters & JWT Validation".
 *
 * The Gateway validates the JWT — services behind it trust the Gateway and
 * never re-validate tokens themselves (this filter IS the security boundary;
 * see Session 3 docx 3.6, Daily Quiz Q6).
 *
 * Runs at HIGHEST_PRECEDENCE + 1 — one step after LoggingFilter
 * (HIGHEST_PRECEDENCE), so every request is logged, including ones this
 * filter rejects with 401.
 *
 * NOTE on PUBLIC_ROUTES: this repo's Product Service uses /api/v1/products
 * (see Session 1/2) — the docx's literal example path is /api/products.
 * The whitelist below matches this repo's actual route, not the docx's
 * shorthand, per the 1:1 fidelity rule with the real running code.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // Routes that do NOT require a JWT token.
    // TODO (Session 3 homework): move this to application.yml via
    // @ConfigurationProperties instead of hardcoding it here.
    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/v1/products"   // GET all products — public
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Step 1: Skip validation for public routes
        if (isPublicRoute(path)) {
            return chain.filter(exchange);
        }

        // Step 2: Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        // Step 3: Validate JWT
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.validateToken(token);

            // Step 4: Enrich request with user info for downstream services
            ServerHttpRequest enrichedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(enrichedRequest).build());
        } catch (JwtException e) {
            return unauthorizedResponse(exchange, "Invalid or expired token: " + e.getMessage());
        }
    }

    private boolean isPublicRoute(String path) {
        return PUBLIC_ROUTES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = "{\"error\": \"" + message + "\", \"status\": 401}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // runs after LoggingFilter
    }
}
