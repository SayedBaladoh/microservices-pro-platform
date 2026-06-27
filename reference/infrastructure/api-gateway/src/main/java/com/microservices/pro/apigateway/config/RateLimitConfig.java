package com.microservices.pro.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * RateLimitConfig — Session 3, "Rate Limiting with Redis".
 *
 * Defines HOW to identify a client for rate limiting. Two strategies:
 *   - ipKeyResolver (@Primary): simplest, used by the product-service route
 *   - userKeyResolver: rate-limits per authenticated user (X-User-Id header,
 *     set by JwtAuthFilter) — available for routes added in later sessions
 *     that need per-user limits instead of per-IP.
 *
 * See Session 3 docx 3.5 Step 3 discussion point: IP-based is simpler but
 * can unfairly limit users behind NAT; user-based is fairer but requires
 * authentication first.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getRemoteAddress()
        ).map(addr -> addr.getAddress().getHostAddress());
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-User-Id")
        ).defaultIfEmpty("anonymous");
    }
}
