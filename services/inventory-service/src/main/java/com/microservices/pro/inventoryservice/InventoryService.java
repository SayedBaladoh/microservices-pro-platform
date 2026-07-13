package com.microservices.pro.inventoryservice;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InventoryService.
 *
 * Implement the TODOs below. See docs/labs/session-06-lab-4a.md and
 * docs/labs/session-07-lab-5a.md.
 */
@Service
public class InventoryService {

    // TODO 1 (Session 6): in-memory store, pre-populated with:
    //   PROD-001: 100 units available
    //   PROD-002: 5 units available
    //   PROD-003: 0 units (out of stock)
    private final Map<String, StockItem> stock = new ConcurrentHashMap<>();

    private final Map<String, Object> reservationsByOrderId = new ConcurrentHashMap<>();

    // TODO 2 (Session 6): checkStock(productId, requestedQty) → StockCheckResponse
    //         Look up the StockItem (default to 0 available if unknown productId),
    //         compute available = item.hasStock(requestedQty), and return the response.
    public StockCheckResponse checkStock(String productId, int requestedQty) {
        throw new UnsupportedOperationException("TODO: implement checkStock()");
    }

    // TODO 3 (Session 7): reserveStock(productId, quantity, orderId)
    //         Throw InsufficientStockException if !item.hasStock(quantity).
    //         Otherwise increase reservedQuantity and remember the
    //         (productId, quantity) for this orderId so releaseStock() can
    //         undo it later.
    public void reserveStock(String productId, int quantity, String orderId) {
        throw new UnsupportedOperationException("TODO: implement reserveStock()");
    }

    // TODO 4 (Session 7): releaseStock(orderId) — compensation.
    //         Look up what this orderId reserved and decrease
    //         reservedQuantity accordingly. Must be idempotent: if nothing
    //         is found for this orderId, do nothing (no exception).
    public void releaseStock(String orderId) {
        throw new UnsupportedOperationException("TODO: implement releaseStock()");
    }
}
