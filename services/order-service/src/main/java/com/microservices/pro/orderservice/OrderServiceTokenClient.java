package com.microservices.pro.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * OrderServiceTokenClient — Session 20, Lab 16.
 * Implement the TODOs. See docs/labs/session-20-lab-16.md.
 */
@Service
public class OrderServiceTokenClient {

    @Value("${keycloak.token-endpoint:http://localhost:8180/realms/ecommerce-platform/protocol/openid-connect/token}")
    private String tokenEndpoint;

    @Value("${keycloak.client-id:order-service}")
    private String clientId;

    @Value("${keycloak.client-secret:order-service-secret-dev-only}")
    private String clientSecret;

    // TODO 1: Add fields for cachedToken (String) and expiresAt (Instant)
    // TODO 2: getAccessToken() — return cached token if not expired (leave 30s buffer)
    //         Otherwise call fetchNewToken()
    // TODO 3: fetchNewToken() — POST to tokenEndpoint with grant_type=client_credentials,
    //         cache the result, return access_token
    //         Use WebClient (not RestTemplate — Gateway is WebFlux context)
    public String getAccessToken() {
        throw new UnsupportedOperationException("TODO: implement getAccessToken()");
    }
}
