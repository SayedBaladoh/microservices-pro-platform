package com.microservices.pro.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Configuration;

/**
 * RateLimitConfig — Session 3, "Rate Limiting with Redis".
 *
 * Implement the TODOs below. See docs/labs/session-03-lab-2b.md.
 */
@Configuration
public class RateLimitConfig {

    // TODO 1: Create an IP-based KeyResolver bean, annotated @Bean and @Primary
    //         Use exchange.getRequest().getRemoteAddress()

    // TODO 2 (BONUS): Create a user-based KeyResolver bean
    //         Use the X-User-Id header, defaulting to "anonymous"

}
