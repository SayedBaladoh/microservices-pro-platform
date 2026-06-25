package com.microservices.pro.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config Server — Session 1.
 *
 * Centralized configuration for every service in the platform.
 * Services pull their config from here at startup via
 * spring.config.import: "optional:configserver:http://localhost:8888"
 *
 * Health check: http://localhost:8888/actuator/health
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
