package com.microservices.pro.inventoryservice;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InventoryService.
 *
 * Session 6 — checkStock(): the synchronous, read-only pre-check called by
 * Order Service via OpenFeign before confirming an order.
 *
 * Session 7 — reserveStock()/releaseStock(): the actual stock mutations
 * driven by the Choreography Saga (InventorySagaHandler), reacting to
 * OrderPlaced / PaymentFailed Kafka events rather than being called
 * directly by another service.
 *
 * In-memory store (ConcurrentHashMap) — documented technical debt: no
 * persistence across restarts. See Session 8 Architecture Clinic
 * questions (docs/trainer/session-08-architecture-clinic-questions.md).
 *
 * Reservation tracking note: releaseStock(orderId) (Session 7's documented
 * signature — the Saga only has the orderId on the PaymentFailed event, not
 * the original productId/quantity) needs to know what THAT specific order
 * reserved. This repo tracks that with a small in-memory
 * reservationsByOrderId map alongside the stock map — itself another
 * documented technical debt item (in-memory, not persisted) consistent
 * with the rest of this class.
 */
@Service
public class InventoryService {

    private final Map<String, StockItem> stock = new ConcurrentHashMap<>(Map.of(
            "PROD-001", new StockItem("PROD-001", 100, 0),
            "PROD-002", new StockItem("PROD-002", 5, 0),
            "PROD-003", new StockItem("PROD-003", 0, 0)  // out of stock
    ));

    private final Map<String, Reservation> reservationsByOrderId = new ConcurrentHashMap<>();

    private record Reservation(String productId, int quantity) {}

    public StockCheckResponse checkStock(String productId, int requestedQty) {
        StockItem item = stock.getOrDefault(productId, new StockItem(productId, 0, 0));
        boolean available = item.hasStock(requestedQty);
        return new StockCheckResponse(productId, requestedQty,
                available, item.availableQuantity() - item.reservedQuantity());
    }

    /**
     * Session 7 — commits a reservation against an already stock-checked
     * order. Throws InsufficientStockException if the stock disappeared
     * between the Session 6 pre-check and this Saga step (e.g. a
     * concurrent order consumed it first) — the Saga's compensation path
     * handles this the same way as any other reservation failure.
     */
    public void reserveStock(String productId, int quantity, String orderId) {
        StockItem item = stock.getOrDefault(productId, new StockItem(productId, 0, 0));
        if (!item.hasStock(quantity)) {
            throw new InsufficientStockException(
                    "Cannot reserve " + quantity + " of " + productId + " for order " + orderId);
        }
        stock.put(productId, new StockItem(
                item.productId(),
                item.availableQuantity(),
                item.reservedQuantity() + quantity
        ));
        reservationsByOrderId.put(orderId, new Reservation(productId, quantity));
    }

    /**
     * Session 7 — compensation: releases the reservation associated with
     * orderId when the Saga fails downstream (PaymentFailed). Matches the
     * docx's single-argument signature exactly — the PaymentFailedEvent
     * only carries orderId, so this method looks up what that order
     * reserved via reservationsByOrderId.
     *
     * If no reservation is found (e.g. duplicate compensation, or this
     * order never successfully reserved anything), this is a no-op — see
     * the idempotency note in Session 7 docx's Common Issues table
     * ("Duplicate compensation (inventory released twice)").
     */
    public void releaseStock(String orderId) {
        Reservation reservation = reservationsByOrderId.remove(orderId);
        if (reservation == null) {
            return; // already released, or nothing was ever reserved — idempotent no-op
        }
        StockItem item = stock.getOrDefault(reservation.productId(), new StockItem(reservation.productId(), 0, 0));
        int newReserved = Math.max(0, item.reservedQuantity() - reservation.quantity());
        stock.put(reservation.productId(), new StockItem(item.productId(), item.availableQuantity(), newReserved));
    }
}

