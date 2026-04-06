package com.pm.ordersystem.client;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.command.CancelOrderCommand;
import com.pm.ordersystem.command.SubmitOrderCommand;
import com.pm.ordersystem.engine.TriagingEngine;
import com.pm.ordersystem.handler.OrderHandler;
import com.pm.ordersystem.manager.OrderManager;
import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderManager orderManager;
    private final OrderAccess orderAccess;
    private final OrderHandler orderHandler;
    private final TriagingEngine triagingEngine;
    private final List<NotificationService> observers;

    public OrderController(OrderManager orderManager,
                           OrderAccess orderAccess,
                           OrderHandler orderHandler,
                           TriagingEngine triagingEngine,
                           List<NotificationService> observers) {
        this.orderManager   = orderManager;
        this.orderAccess    = orderAccess;
        this.orderHandler   = orderHandler;
        this.triagingEngine = triagingEngine;
        this.observers      = observers;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderManager.getAllOrders();
    }

    @GetMapping("/pending")
    public List<Order> getPendingOrders() {
        return orderManager.getPendingOrders();
    }

    @GetMapping("/inprogress")
    public List<Order> getInProgressOrders() {
        return orderManager.getInProgressOrders();
    }

    @PostMapping
    public ResponseEntity<?> submitOrder(
            @RequestBody Map<String, String> body) {
        try {
            OrderType type    = OrderType.valueOf(
                    body.get("type").toUpperCase());
            Priority priority = Priority.valueOf(
                    body.get("priority").toUpperCase());

            SubmitOrderCommand cmd = new SubmitOrderCommand(
                    type,
                    body.get("patientName"),
                    body.get("clinician"),
                    body.get("description"),
                    priority,
                    orderAccess,
                    orderHandler,
                    triagingEngine,
                    observers
            );

            Order order = orderManager.handle(cmd);
            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            CancelOrderCommand cmd = new CancelOrderCommand(
                    id,
                    body.get("actor"),
                    orderAccess,
                    observers
            );
            Order order = orderManager.handle(cmd);
            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException |
                 IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}