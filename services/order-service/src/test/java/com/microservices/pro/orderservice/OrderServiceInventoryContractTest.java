package com.microservices.pro.orderservice;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * OrderServiceInventoryContractTest — Session 11, Lab 9B, Task 1.
 *
 * Consumer-Driven Contract test. Generates target/pacts/order-service-inventory-service.json.
 *
 * Implement the TODOs below. See docs/labs/session-11-lab-9b.md.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "inventory-service", port = "8888")
class OrderServiceInventoryContractTest {

    // TODO 1: Define a @Pact method for "PROD-001 has 100 units in stock"
    //         Use LambdaDsl.newJsonBody to define: available (boolean), remainingStock (integer), productId (string)
    //         The field NAMES in the body are the contract — changing them = breaking the provider.
    @Pact(consumer = "order-service", provider = "inventory-service")
    RequestResponsePact checkStockAvailable(PactDslWithProvider builder) {
        throw new UnsupportedOperationException("TODO: define the @Pact interaction for stock available");
    }

    @Test
    @PactTestFor(pactMethod = "checkStockAvailable")
    void checkStock_deserializesAvailableField_correctly(MockServer mockServer) {
        // TODO 2: Build an InventoryClient pointing at mockServer.getUrl()
        // TODO 3: Call client.checkStock("PROD-001", 5) and assert response.available() == true
        throw new UnsupportedOperationException("TODO: implement this Pact consumer test");
    }

    // TODO 4 (bonus): Add a second @Pact + @Test for the 409 (out-of-stock) case
}
