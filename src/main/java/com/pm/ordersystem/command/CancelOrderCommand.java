package com.pm.ordersystem.command;

import com.pm.ordersystem.access.ClinicianAccess;
import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;

import java.util.List;

public class CancelOrderCommand implements Command {

    private final String orderId;
    private final String actor;
    private final OrderAccess orderAccess;
    private final ClinicianAccess clinicianAccess;
    private final List<NotificationService> observers;

    public CancelOrderCommand(String orderId,
                              String actor,
                              OrderAccess orderAccess,
                              ClinicianAccess clinicianAccess,
                              List<NotificationService> observers) {
        this.orderId         = orderId;
        this.actor           = actor;
        this.orderAccess     = orderAccess;
        this.clinicianAccess = clinicianAccess;
        this.observers       = observers;
    }

    @Override
    public void execute() {
        // 1. validate actor is a registered clinician
        if (!clinicianAccess.exists(actor)) {
            throw new IllegalArgumentException(
                    "Only a registered clinician can cancel an order. "
                            + actor + " is not a registered clinician.");
        }

        // 2. find the order
        Order order = orderAccess.findOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderId));

        // 3. validate actor is the clinician who submitted
        if (!order.getClinician().equals(actor)) {
            throw new IllegalArgumentException(
                    "Only the clinician who submitted this order "
                            + "can cancel it. Submitted by: "
                            + order.getClinician());
        }

        // 4. validate status is PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order cannot be cancelled — status is "
                            + order.getStatus()
                            + ". Only PENDING orders can be cancelled.");
        }

        // 5. cancel it
        order.setStatus(OrderStatus.CANCELLED);
        orderAccess.saveOrder(order);

        // 6. notify
        observers.forEach(o ->
                o.onOrderStatusChanged(order, "CANCELLED"));
    }

    public String getOrderId() { return orderId; }
    public String getActor()   { return actor; }
}