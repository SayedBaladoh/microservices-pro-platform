package com.microservices.pro.inventoryservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InventoryServiceTest — Session 6, Lab 4A, Task 4.
 *
 * Matches the original lab spec's required tests:
 *   Test 1: checkStock() returns available=true when stock sufficient
 *           (productId=PROD-001, quantity=10 → available=true)
 *   Test 2: checkStock() returns available=false when stock insufficient
 *           (productId=PROD-003, quantity=1 → available=false)
 *   Test 3: hasStock() logic in StockItem
 *           new StockItem("X", 10, 3).hasStock(5) → true (10-3=7 >= 5)
 *           new StockItem("X", 10, 3).hasStock(8) → false (10-3=7 < 8)
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void checkStock_returnsAvailableTrue_whenStockSufficient() {
        StockCheckResponse response = inventoryService.checkStock("PROD-001", 10);

        assertThat(response.available()).isTrue();
        assertThat(response.remainingStock()).isEqualTo(100);
    }

    @Test
    void checkStock_returnsAvailableFalse_whenStockInsufficient() {
        StockCheckResponse response = inventoryService.checkStock("PROD-003", 1);

        assertThat(response.available()).isFalse();
    }

    @Test
    void hasStock_accountsForReservedQuantity() {
        StockItem item = new StockItem("X", 10, 3);

        assertThat(item.hasStock(5)).isTrue();   // 10-3=7 >= 5
        assertThat(item.hasStock(8)).isFalse();  // 10-3=7 < 8
    }
}
