package com.microservices.pro.orderservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderSagaTest — Session 7, Lab 5A, Task 5.
 *
 * Matches the original lab spec's required tests:
 *   Test 1: createOrder() saves order with PENDING status
 *           (verify: orderRepository.save() called with status=PENDING)
 *   Test 2: createOrder() publishes OrderPlacedEvent to "order-events"
 *           (verify: kafkaTemplate.send("order-events", ...) called)
 *   Test 3: handlePaymentFailed() updates order to PAYMENT_FAILED
 *           (call the method with a PaymentFailedEvent — verify status update)
 *
 * Test 3 is exercised on OrderSagaEventHandler (where the docx's own code
 * actually places handlePaymentEvent — see OrderSagaEventHandler.java),
 * not on OrderService, since that is where this repo's
 * @KafkaListener-annotated handling logic lives.
 */
@ExtendWith(MockitoExtension.class)
class OrderSagaTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Test
    void createOrder_savesOrderWithPendingStatus() {
        OrderRequest request = new OrderRequest("PROD-001", 2, new BigDecimal("200.00"), "cust-1");
        when(inventoryClient.checkStock("PROD-001", 2))
                .thenReturn(new StockCheckResponse("PROD-001", 2, true, 98));

        orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void createOrder_publishesOrderPlacedEvent_toOrderEventsTopic() {
        OrderRequest request = new OrderRequest("PROD-001", 2, new BigDecimal("200.00"), "cust-1");
        when(inventoryClient.checkStock("PROD-001", 2))
                .thenReturn(new StockCheckResponse("PROD-001", 2, true, 98));

        orderService.createOrder(request);

        verify(kafkaTemplate).send(
                org.mockito.ArgumentMatchers.eq("order-events"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(com.microservices.pro.orderservice.events.OrderPlacedEvent.class));
    }
}
