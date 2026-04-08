package com.pm.ordersystem.client;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.command.ClaimOrderCommand;
import com.pm.ordersystem.command.CompleteOrderCommand;
import com.pm.ordersystem.manager.OrderManager;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class FulfilmentController {

    private final OrderManager orderManager;
    private final OrderAccess orderAccess;
    private final StaffAccess staffAccess;
    private final List<NotificationService> observers;

    public FulfilmentController(OrderManager orderManager,
                                OrderAccess orderAccess,
                                StaffAccess staffAccess,
                                List<NotificationService> observers) {
        this.orderManager = orderManager;
        this.orderAccess  = orderAccess;
        this.staffAccess  = staffAccess;
        this.observers    = observers;
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<?> claimOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            ClaimOrderCommand cmd = new ClaimOrderCommand(
                    id,
                    body.get("claimedBy"),
                    orderAccess,
                    staffAccess,
                    observers
            );
            Order order = orderManager.handle(cmd);
            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException |
                 IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            CompleteOrderCommand cmd = new CompleteOrderCommand(
                    id,
                    body.get("actor"),
                    orderAccess,
                    staffAccess,
                    observers
            );
            Order order = orderManager.handle(cmd);
            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException |
                 IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}