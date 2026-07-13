package com.microservices.pro.orderservice;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * FeignJwtInterceptor — Session 6.
 *
 * Implement the TODO below. See docs/labs/session-06-lab-4a.md.
 */
@Component
public class FeignJwtInterceptor implements RequestInterceptor {

    // TODO: Read the Authorization header from the current incoming
    //       request (RequestContextHolder.getRequestAttributes(), cast to
    //       ServletRequestAttributes) and forward it onto every outgoing
    //       Feign call via template.header(...).
    @Override
    public void apply(RequestTemplate template) {
        throw new UnsupportedOperationException("TODO: implement apply()");
    }
}
