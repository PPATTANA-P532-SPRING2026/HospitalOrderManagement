package com.pm.ordersystem.client;

import com.pm.ordersystem.command.CancelOrderCommand;
import com.pm.ordersystem.command.SubmitOrderCommand;
import com.pm.ordersystem.manager.OrderManager;
import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderManager orderManager;

    public OrderController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    //   get all orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderManager.getAllOrders();
    }

    //  get pending orders sorted by triage
    @GetMapping("/queue")
    public List<Order> getQueue() {
        return orderManager.getPendingOrders();
    }

    //  get in progress orders
    @GetMapping("/inprogress")
    public List<Order> getInProgress() {
        return orderManager.getInProgressOrders();
    }

    // submit a new order
    @PostMapping
    public ResponseEntity<?> submitOrder(
            @RequestBody Map<String, String> body) {
        try {
            OrderType type  = OrderType.valueOf(
                    body.get("type").toUpperCase());
            Priority priority = Priority.valueOf(
                    body.get("priority").toUpperCase());

            SubmitOrderCommand cmd = new SubmitOrderCommand(
                    type,
                    body.get("patientName"),
                    body.get("clinician"),
                    body.get("description"),
                    priority
            );

            Order order = orderManager.handle(cmd);
            return ResponseEntity.ok(order);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  cancel a pending order
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            CancelOrderCommand cmd = new CancelOrderCommand(
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