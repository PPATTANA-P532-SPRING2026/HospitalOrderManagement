package com.pm.ordersystem.client;

import com.pm.ordersystem.command.CommandLogEntry;
import com.pm.ordersystem.manager.OrderManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final OrderManager orderManager;

    public AuditController(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    // ── GET /api/audit — get full audit trail ─────────────────────────
    @GetMapping
    public List<CommandLogEntry> getAuditLog() {
        return orderManager.getAuditLog();
    }
}
