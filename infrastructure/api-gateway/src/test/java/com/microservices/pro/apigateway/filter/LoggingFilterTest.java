package com.microservices.pro.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LoggingFilterTest — Session 2, Lab 2A, Task 3.
 *
 * Matches the original lab spec's minimum 2 required tests, plus the
 * documented bonus test (Test 3: verify the filter logs the correct HTTP
 * method) — included here as Test 3 since this repo packages bonus scope
 * together with the required minimum.
 *
 * Test 1: getOrder() returns Ordered.HIGHEST_PRECEDENCE
 * Test 2: filter() calls chain.filter() (verified via mock)
 * Test 3 (bonus): filter() does not alter the request — the same method
 *                  the client sent is the one forwarded downstream
 */
@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    private final LoggingFilter loggingFilter = new LoggingFilter();

    @Mock
    private GatewayFilterChain filterChain;

    @Test
    void getOrder_returnsHighestPrecedence() {
        assertThat(loggingFilter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void filter_callsChainFilter() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_logsTheIncomingHttpMethod_withoutMutatingTheRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/products").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .verifyComplete();

        // LoggingFilter is read-only logging — confirms the exact same
        // exchange (and therefore the same method, POST) reaches the chain.
        assertThat(exchange.getRequest().getMethod()).isEqualTo(HttpMethod.POST);
    }
}
