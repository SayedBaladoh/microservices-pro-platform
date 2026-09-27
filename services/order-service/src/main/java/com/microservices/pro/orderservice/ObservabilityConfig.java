package com.microservices.pro.orderservice;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ObservabilityConfig — Session 17, Lab 13A.
 *
 * Registers TimedAspect so @Timed on createOrder() and startSaga()
 * emits timing metrics to /actuator/prometheus.
 *
 * spring-boot-starter-aop already on classpath from Session 4.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
