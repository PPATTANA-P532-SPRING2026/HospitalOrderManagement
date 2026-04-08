package com.pm.ordersystem.command;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;

import java.util.List;

public class ClaimOrderCommand implements Command {

    private final String orderId;
    private final String claimedBy;
    private final OrderAccess orderAccess;
    private final StaffAccess staffAccess;
    private final List<NotificationService> observers;

    public ClaimOrderCommand(String orderId,
                             String claimedBy,
                             OrderAccess orderAccess,
                             StaffAccess staffAccess,
                             List<NotificationService> observers) {
        this.orderId     = orderId;
        this.claimedBy   = claimedBy;
        this.orderAccess = orderAccess;
        this.staffAccess = staffAccess;
        this.observers   = observers;
    }

    @Override
    public void execute() {
        // 1. find the order first
        Order order = orderAccess.findOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderId));

        // 2. validate status is PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order cannot be claimed — status is "
                            + order.getStatus());
        }

        // 3. resolve who is claiming
        String resolvedClaimedBy = claimedBy;

        if ("AUTO".equals(claimedBy)) {
            // load balancing — use whoever is pre-assigned
            if (order.getClaimedBy() != null) {
                resolvedClaimedBy = order.getClaimedBy();
            } else {
                throw new IllegalStateException(
                        "No staff assigned to this order yet.");
            }
        } else {
            // manual claim — validate staff is registered
            if (!staffAccess.exists(claimedBy)) {
                throw new IllegalArgumentException(
                        "Only registered fulfilment staff can claim orders. "
                                + claimedBy + " is not registered as staff.");
            }

            // if pre-assigned check it matches
            if (order.getClaimedBy() != null
                    && !order.getClaimedBy().equals(claimedBy)) {
                throw new IllegalArgumentException(
                        "This order is assigned to "
                                + order.getClaimedBy()
                                + ". Only the assigned staff can claim it.");
            }
        }

        // 4. claim it
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy(resolvedClaimedBy);
        orderAccess.saveOrder(order);

        // 5. notify
        final String finalClaimedBy = resolvedClaimedBy;
        observers.forEach(o ->
                o.onOrderStatusChanged(order, "CLAIMED"));
    }

    public String getOrderId()   { return orderId; }
    public String getClaimedBy() { return claimedBy; }
}