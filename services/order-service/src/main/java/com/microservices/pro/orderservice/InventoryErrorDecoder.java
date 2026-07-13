package com.microservices.pro.orderservice;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

/**
 * InventoryErrorDecoder — Session 6.
 *
 * By default, Feign wraps all HTTP errors as FeignException. This
 * translates them into meaningful domain exceptions.
 */
@Component
public class InventoryErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 409 -> new InsufficientStockException(
                    "Product out of stock — response: " + response.status());
            case 404 -> new ProductNotFoundException(
                    "Product not found in Inventory Service");
            case 503 -> new ServiceUnavailableException(
                    "Inventory Service temporarily unavailable");
            default -> new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}
