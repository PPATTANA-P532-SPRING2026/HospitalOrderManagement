package com.pm.ordersystem.client;

import com.pm.ordersystem.manager.OrderManager;
import com.pm.ordersystem.notification.ConsoleNotificationService;
import com.pm.ordersystem.notification.EmailNotificationService;
import com.pm.ordersystem.notification.InAppAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final OrderManager orderManager;
    private final ConsoleNotificationService consoleService;
    private final InAppAlertService inAppAlertService;
    private final EmailNotificationService emailService;

    public NotificationController(
            OrderManager orderManager,
            ConsoleNotificationService consoleService,
            InAppAlertService inAppAlertService,
            EmailNotificationService emailService) {
        this.orderManager      = orderManager;
        this.consoleService    = consoleService;
        this.inAppAlertService = inAppAlertService;
        this.emailService      = emailService;
    }

    // ── GET /api/notifications/badge
    @GetMapping("/badge")
    public ResponseEntity<?> getBadge() {
        return ResponseEntity.ok(Map.of(
                "badgeCount", inAppAlertService.getBadgeCount()
        ));
    }

    // ── POST /api/notifications/badge/reset
    @PostMapping("/badge/reset")
    public ResponseEntity<?> resetBadge() {
        inAppAlertService.resetBadgeCount();
        return ResponseEntity.ok(Map.of("badgeCount", 0));
    }

    // ── POST /api/notifications/channels
    @PostMapping("/channels")
    public ResponseEntity<?> setChannels(
            @RequestBody Map<String, List<String>> body) {
        List<String> channels = body.get("channels");

        // clear current observers
        orderManager.clearObservers();

        // register only selected channels
        if (channels.contains("console")) {
            orderManager.register(consoleService);
        }
        if (channels.contains("inapp")) {
            orderManager.register(inAppAlertService);
        }
        if (channels.contains("email")) {
            orderManager.register(emailService);
        }

        return ResponseEntity.ok(Map.of(
                "channels", channels,
                "message",  "Notification channels updated"
        ));
    }
}