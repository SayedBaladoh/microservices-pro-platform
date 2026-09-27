package com.microservices.pro.apigateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * UserContextEnrichmentFilter — Session 20.
 *
 * Replaces JwtAuthFilter's X-User-Id / X-User-Role header injection.
 * The trust-boundary PATTERN from Session 3 is preserved:
 *   Gateway validates identity → downstream services receive pre-validated headers.
 * Only the SOURCE changed: Session 3 parsed the JWT manually; this filter
 * reads from Spring Security's authenticated principal (already validated by
 * the OAuth2 Resource Server).
 *
 * Downstream services (Order, Inventory, etc.) never need to import Spring
 * Security — they simply trust the X-User-Id/X-User-Role headers the Gateway
 * already validated and forwarded.
 *
 * ORDER = -1: runs AFTER Spring Security authentication (which defaults to
 * Ordered.HIGHEST_PRECEDENCE) and BEFORE route forwarding.
 */
@Component
public class UserContextEnrichmentFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    if (ctx.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = (Jwt) jwtAuth.getPrincipal();
                        String userId = jwt.getSubject();
                        String role = jwtAuth.getAuthorities().stream()
                                .findFirst()
                                .map(a -> a.getAuthority().replace("ROLE_", ""))
                                .orElse("USER");

                        ServerWebExchange mutated = exchange.mutate()
                                .request(r -> r.headers(headers -> {
                                    headers.set("X-User-Id", userId);
                                    headers.set("X-User-Role", role);
                                }))
                                .build();
                        return chain.filter(mutated);
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
