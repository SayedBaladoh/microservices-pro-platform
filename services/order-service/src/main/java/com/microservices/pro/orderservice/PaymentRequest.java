package com.microservices.pro.orderservice;

import java.math.BigDecimal;

/**
 * PaymentRequest — Session 4.
 *
 * Deliberately duplicated from payment-service's own PaymentRequest record
 * rather than shared via a common library. This repo's services do not
 * share a domain-model library yet — that consolidation is Session 14
 * scope. Keeping each service's copy independent for now is the documented
 * approach (see Session 6 docx for the same pattern applied to Feign DTOs).
 */
public record PaymentRequest(BigDecimal amount) {}
