package com.pm.ordersystem.client;

import com.pm.ordersystem.command.CommandLogEntry;
import com.pm.ordersystem.manager.OrderManager;
import com.pm.ordersystem.model.order.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final OrderManager orderManager;

    public AuditController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    @GetMapping
    public List<CommandLogEntry> getAuditLog() {
        return orderManager.getAuditLog();
    }

    @PostMapping("/undo")
    public ResponseEntity<?> undoLast() {
        try {
            orderManager.undoLast();
            return ResponseEntity.ok("Last command undone");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/replay/{id}")
    public ResponseEntity<?> replay(@PathVariable String id) {
        try {
            Order order = orderManager.replay(id);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}