package com.microservices.pro.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * LoggingFilter — Session 2, Lab 2A, Task 2.
 *
 * GlobalFilter applied to ALL routes (not route-specific — see Session 2
 * docx 3.6 GatewayFilter vs GlobalFilter comparison). Logs the request
 * method/path/remote-address before forwarding, and the response status
 * after the downstream call completes.
 *
 * Runs at Ordered.HIGHEST_PRECEDENCE so it captures every request,
 * including ones later rejected by filters added in Session 3
 * (JwtAuthFilter runs at HIGHEST_PRECEDENCE + 1 — after this one).
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("[GATEWAY] {} {} from {}",
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            log.info("[GATEWAY] Response status: {}", response.getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // runs first
    }
}
