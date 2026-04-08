package com.pm.ordersystem.command;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;

import java.util.List;

public class CompleteOrderCommand implements Command {

    private final String orderId;
    private final String actor;
    private final OrderAccess orderAccess;
    private final StaffAccess staffAccess;
    private final List<NotificationService> observers;

    public CompleteOrderCommand(String orderId,
                                String actor,
                                OrderAccess orderAccess,
                                StaffAccess staffAccess,
                                List<NotificationService> observers) {
        this.orderId     = orderId;
        this.actor       = actor;
        this.orderAccess = orderAccess;
        this.staffAccess = staffAccess;
        this.observers   = observers;
    }

    @Override
    public void execute() {
        // 1. validate actor is registered staff
        if (!staffAccess.exists(actor)) {
            throw new IllegalArgumentException(
                    "Only registered fulfilment staff can complete orders. "
                            + actor + " is not registered as staff.");
        }

        // 2. find the order
        Order order = orderAccess.findOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderId));

        // 3. validate status is IN_PROGRESS
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Order cannot be completed — status is "
                            + order.getStatus());
        }

        // 4. validate actor is the staff who claimed it
        if (!actor.equals(order.getClaimedBy())) {
            throw new IllegalArgumentException(
                    "Order can only be completed by the staff "
                            + "member who claimed it. Claimed by: "
                            + order.getClaimedBy());
        }

        // 5. complete it
        order.setStatus(OrderStatus.COMPLETED);
        orderAccess.saveOrder(order);

        // 6. notify
        observers.forEach(o ->
                o.onOrderStatusChanged(order, "COMPLETED"));
    }

    public String getOrderId() { return orderId; }
    public String getActor()   { return actor; }
}