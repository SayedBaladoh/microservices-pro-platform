package com.microservices.pro.inventoryservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

/**
 * InventoryController — Session 6.
 *
 * Implement the TODO below. See docs/labs/session-06-lab-4a.md.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // TODO: GET /api/v1/inventory/check?productId=X&quantity=Y
    //       Returns 200 OK when available=true, 409 Conflict when available=false
    @GetMapping("/check")
    public ResponseEntity<StockCheckResponse> checkStock(
            @RequestParam String productId,
            @RequestParam int quantity) {
        throw new UnsupportedOperationException("TODO: implement checkStock()");
    }
}
