package com.microservices.pro.productservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductCommandServiceTest — Session 18, Lab 13B, Task 3.
 *
 * Test 1: createProduct() saves product AND publishes ProductChangedEvent
 *         with changeType "CREATED".
 * Test 2: createProduct() with price <= 0 throws IllegalArgumentException
 *         (invariant enforcement on the Command side).
 *
 * Note on @Mock ApplicationEventPublisher:
 *   Must be present alongside @Mock ProductRepository, or Mockito cannot
 *   inject it into ProductCommandService and throws NullPointerException
 *   when publishEvent() is called. See Common Issues §4.
 */
@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductCommandService commandService;

    @Test
    void createProduct_savesProduct_andPublishesCreatedEvent() {
        Product saved = new Product(1L, "Laptop Pro", "High-end laptop",
                new BigDecimal("1299.99"), "ELECTRONICS");
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = commandService.createProduct(
                "Laptop Pro", "High-end laptop",
                new BigDecimal("1299.99"), "ELECTRONICS");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop Pro");

        ArgumentCaptor<ProductChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(ProductChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().changeType()).isEqualTo("CREATED");
        assertThat(eventCaptor.getValue().productId()).isEqualTo(1L);
    }

    @Test
    void createProduct_throwsException_whenPriceIsZeroOrNegative() {
        assertThatThrownBy(() ->
                commandService.createProduct("Laptop", "desc",
                        BigDecimal.ZERO, "ELECTRONICS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("price must be greater than zero");
    }
}
