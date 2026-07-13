package com.microservices.pro.orderservice;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

/**
 * InventoryErrorDecoder — Session 6.
 *
 * Implement the TODO below. See docs/labs/session-06-lab-4a.md.
 */
@Component
public class InventoryErrorDecoder implements ErrorDecoder {

    // TODO: Translate HTTP status codes to domain exceptions:
    //   409 -> InsufficientStockException
    //   404 -> ProductNotFoundException
    //   503 -> ServiceUnavailableException
    //   default -> fall back to new ErrorDecoder.Default().decode(methodKey, response)
    @Override
    public Exception decode(String methodKey, Response response) {
        // Placeholder so this class compiles before the TODO above is done —
        // replace this with the real switch on response.status().
        return new UnsupportedOperationException("TODO: implement decode() for status " + response.status());
    }
}
