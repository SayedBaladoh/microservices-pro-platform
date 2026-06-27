package com.microservices.pro.apigateway.filter;

import com.microservices.pro.apigateway.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * JwtAuthFilterTest — Session 3, Lab 2B, Task 3.
 *
 * Matches the original lab spec's required tests:
 *   Test 4: getOrder() returns HIGHEST_PRECEDENCE + 1
 *   Test 5: filter() calls chain.filter() for public routes
 *           (mock ServerWebExchange with path "/api/v1/products" — this
 *           repo's actual public route, see JwtAuthFilter.PUBLIC_ROUTES)
 *
 * Public-route requests never touch JwtUtil at all (no Authorization
 * header is even read) — verifyNoInteractions confirms the filter takes
 * the early-return path, not just that *some* path eventually calls chain.filter().
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain filterChain;

    @Test
    void getOrder_returnsHighestPrecedencePlusOne() {
        assertThat(jwtAuthFilter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
    }

    @Test
    void filter_callsChainFilter_forPublicRoutes_withoutValidatingAnyToken() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
        verifyNoInteractions(jwtUtil);
    }
}
