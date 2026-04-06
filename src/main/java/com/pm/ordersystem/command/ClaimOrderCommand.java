package com.pm.ordersystem.command;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;

import java.util.List;

public class ClaimOrderCommand implements Command {

    // ── data ──────────────────────────────────────────────────────────
    private final String orderId;
    private final String claimedBy;

    // ── dependencies ──────────────────────────────────────────────────
    private final OrderAccess orderAccess;
    private final List<NotificationService> observers;

    public ClaimOrderCommand(String orderId,
                             String claimedBy,
                             OrderAccess orderAccess,
                             List<NotificationService> observers) {
        this.orderId     = orderId;
        this.claimedBy   = claimedBy;
        this.orderAccess = orderAccess;
        this.observers   = observers;
    }

    @Override
    public void execute() {
        // 1. find the order
        Order order = orderAccess.findOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderId));

        // 2. validate
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order cannot be claimed — status is "
                            + order.getStatus());
        }

        // 3. claim it
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy(claimedBy);
        orderAccess.saveOrder(order);

        // 4. notify
        observers.forEach(o ->
                o.onOrderStatusChanged(order, "CLAIMED"));
    }

    public String getOrderId()   { return orderId; }
    public String getClaimedBy() { return claimedBy; }
}