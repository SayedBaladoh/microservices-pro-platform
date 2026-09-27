package com.microservices.pro.productservice;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ObservabilityConfig — Session 17, Lab 13A.
 *
 * Enables @Timed annotation support. Spring Boot registers the MeterRegistry
 * automatically (via micrometer-registry-prometheus on the classpath), but
 * @Timed requires a TimedAspect bean to intercept annotated methods via AOP.
 *
 * Without this bean: @Timed annotations are silently ignored — no timing
 * metrics appear in /actuator/prometheus. This is the most common S17
 * mistake. See Session 17 docx Common Issues table.
 *
 * Requires: spring-boot-starter-aop (already on the classpath from Phase 1).
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
