package com.pm.ordersystem.command;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;

import java.util.List;

public class CompleteOrderCommand implements Command {

    // ── data
    private final String orderId;
    private final String actor;

    // ── dependencies
    private final OrderAccess orderAccess;
    private final List<NotificationService> observers;

    public CompleteOrderCommand(String orderId,
                                String actor,
                                OrderAccess orderAccess,
                                List<NotificationService> observers) {
        this.orderId     = orderId;
        this.actor       = actor;
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
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Order cannot be completed — status is "
                            + order.getStatus());
        }

        // 3. complete it
        order.setStatus(OrderStatus.COMPLETED);
        orderAccess.saveOrder(order);

        // 4. notify
        observers.forEach(o ->
                o.onOrderStatusChanged(order, "COMPLETED"));
    }

    public String getOrderId() { return orderId; }
    public String getActor()   { return actor; }
}