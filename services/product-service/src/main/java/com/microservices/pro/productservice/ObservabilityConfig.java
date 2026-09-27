package com.microservices.pro.productservice;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ObservabilityConfig — Session 17, Lab 13A.
 *
 * Implement the TODO below. See docs/labs/session-17-lab-13a.md.
 */
@Configuration
public class ObservabilityConfig {

    // TODO: Return a new TimedAspect(registry).
    //   Without this bean, @Timed annotations on ProductService methods
    //   are silently ignored — no timing metrics appear in /actuator/prometheus.
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        throw new UnsupportedOperationException("TODO: implement timedAspect()");
    }
}
