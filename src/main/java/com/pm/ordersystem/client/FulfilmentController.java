package com.pm.ordersystem.client;

import com.pm.ordersystem.command.ClaimOrderCommand;
import com.pm.ordersystem.command.CompleteOrderCommand;
import com.pm.ordersystem.manager.OrderManager;
import com.pm.ordersystem.model.order.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class FulfilmentController {

    private final OrderManager orderManager;

    public FulfilmentController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    // claim an order
    @PostMapping("/{id}/claim")
    public ResponseEntity<?> claimOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            ClaimOrderCommand cmd = new ClaimOrderCommand(
                    id,
                    body.get("claimedBy")
            );
            Order order = orderManager.handle(cmd);
            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  complete an order
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            CompleteOrderCommand cmd = new CompleteOrderCommand(
                    id,
                    body.get("actor")
            );
            Order order = orderManager.handle(cmd);
            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}