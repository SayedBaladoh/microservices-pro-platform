package com.microservices.pro.apigateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * UserContextEnrichmentFilter — Session 20, Lab 16.
 * Implement the TODO. See docs/labs/session-20-lab-16.md.
 */
@Component
public class UserContextEnrichmentFilter implements GlobalFilter, Ordered {

    // TODO: Read from ReactiveSecurityContextHolder, extract sub (userId)
    //       and first authority (role), set X-User-Id + X-User-Role headers
    //       on the mutated request before forwarding.
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        throw new UnsupportedOperationException("TODO: implement UserContextEnrichmentFilter");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
