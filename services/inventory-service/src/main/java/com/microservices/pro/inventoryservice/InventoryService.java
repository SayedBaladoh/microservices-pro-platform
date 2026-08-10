package com.microservices.pro.inventoryservice;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InventoryService.
 *
 * Session 6  — checkStock()
 * Session 7  — reserveStock() / releaseStock()
 * Session 11 — resetForTesting() (package-private, for Pact @State setup only)
 */
@Service
public class InventoryService {

    private final Map<String, StockItem> stock = new ConcurrentHashMap<>(Map.of(
            "PROD-001", new StockItem("PROD-001", 100, 0),
            "PROD-002", new StockItem("PROD-002", 5, 0),
            "PROD-003", new StockItem("PROD-003", 0, 0)
    ));

    private final Map<String, Reservation> reservationsByOrderId = new ConcurrentHashMap<>();

    private record Reservation(String productId, int quantity) {}

    public StockCheckResponse checkStock(String productId, int requestedQty) {
        StockItem item = stock.getOrDefault(productId, new StockItem(productId, 0, 0));
        boolean available = item.hasStock(requestedQty);
        return new StockCheckResponse(productId, requestedQty,
                available, item.availableQuantity() - item.reservedQuantity());
    }

    public void reserveStock(String productId, int quantity, String orderId) {
        StockItem item = stock.getOrDefault(productId, new StockItem(productId, 0, 0));
        if (!item.hasStock(quantity)) {
            throw new InsufficientStockException(
                    "Cannot reserve " + quantity + " of " + productId + " for order " + orderId);
        }
        stock.put(productId, new StockItem(
                item.productId(), item.availableQuantity(), item.reservedQuantity() + quantity));
        reservationsByOrderId.put(orderId, new Reservation(productId, quantity));
    }

    public void releaseStock(String orderId) {
        Reservation reservation = reservationsByOrderId.remove(orderId);
        if (reservation == null) return;
        StockItem item = stock.getOrDefault(reservation.productId(), new StockItem(reservation.productId(), 0, 0));
        int newReserved = Math.max(0, item.reservedQuantity() - reservation.quantity());
        stock.put(reservation.productId(), new StockItem(item.productId(), item.availableQuantity(), newReserved));
    }

    /**
     * Session 11 — Pact @State setup helper.
     *
     * Package-private intentionally — only used by InventoryServicePactVerificationTest
     * to set the in-memory stock to a known state before each Pact interaction is
     * verified. MUST NOT be called from production code.
     *
     * If this service ever moves to a persistent store (JPA), this method will need
     * to call the repository instead of mutating the in-memory map directly.
     */
    void resetForTesting(String productId, int available, int reserved) {
        stock.put(productId, new StockItem(productId, available, reserved));
    }
}
