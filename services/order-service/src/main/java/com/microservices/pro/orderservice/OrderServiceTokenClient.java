package com.microservices.pro.orderservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

/**
 * OrderServiceTokenClient — Session 20.
 *
 * Obtains a Keycloak access_token for order-service via the Client Credentials
 * Flow (no user, no redirect, no browser). Used when order-service needs to
 * call inventory-service in a background/async context where no customer JWT
 * is available to propagate.
 *
 * TOKEN CACHING: a new token is only requested when the cached one expires
 * (or on the first call). Without caching, every Feign call would trigger
 * a Keycloak round-trip — unnecessary overhead and a potential rate-limit risk.
 *
 * This bean is @Service (singleton scope) — the cached token and expiry
 * fields persist across calls on the same instance. If this were
 * prototype-scoped, caching would be silently broken (new instance = null
 * cache every call). See Common Issues §5, Session 20 docx.
 *
 * NOTE: this client is used on the ASYNC/BACKGROUND path only (where no
 * customer JWT is in context). For customer-context calls, FeignJwtInterceptor
 * (Session 6) still propagates the customer's token — no change needed there.
 */
@Service
public class OrderServiceTokenClient {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceTokenClient.class);

    @Value("${keycloak.token-endpoint:http://localhost:8180/realms/ecommerce-platform/protocol/openid-connect/token}")
    private String tokenEndpoint;

    @Value("${keycloak.client-id:order-service}")
    private String clientId;

    @Value("${keycloak.client-secret:order-service-secret-dev-only}")
    private String clientSecret;

    private final WebClient webClient = WebClient.create();

    private String cachedToken = null;
    private Instant expiresAt = Instant.MIN;

    public String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(expiresAt.minusSeconds(30))) {
            return cachedToken;
        }
        log.info("[CLIENT-CREDENTIALS] Requesting new token for {}", clientId);
        return fetchNewToken();
    }

    @SuppressWarnings("unchecked")
    private String fetchNewToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        Map<String, Object> response = webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Failed to obtain access_token from Keycloak");
        }

        cachedToken = (String) response.get("access_token");
        int expiresIn = (Integer) response.getOrDefault("expires_in", 300);
        expiresAt = Instant.now().plusSeconds(expiresIn);

        log.info("[CLIENT-CREDENTIALS] Token obtained, expires in {}s", expiresIn);
        return cachedToken;
    }
}
