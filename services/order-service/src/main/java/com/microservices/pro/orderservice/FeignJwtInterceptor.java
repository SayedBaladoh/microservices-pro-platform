package com.microservices.pro.orderservice;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * FeignJwtInterceptor — Session 6.
 *
 * Order Service receives a JWT from the Gateway. This forwards it to every
 * outgoing Feign call automatically, so downstream services (Inventory
 * Service) also know who the user is without re-validating the token
 * themselves — the Gateway remains the single trust boundary (see Session
 * 3's JwtAuthFilter and Session 3 docx 3.6).
 */
@Component
public class FeignJwtInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String authHeader = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null) {
                template.header(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }
    }
}
