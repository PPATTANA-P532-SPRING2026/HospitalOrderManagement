package com.pm.ordersystem.handler;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class PriorityEscalationDecorator implements OrderHandler {

    private final OrderHandler wrapped;
    private final Clock clock;
    private final OrderAccess orderAccess;

    private static final long ESCALATION_WINDOW_MINUTES = 5;

    public PriorityEscalationDecorator(OrderHandler wrapped,
                                       Clock clock,
                                       OrderAccess orderAccess) {
        this.wrapped     = wrapped;
        this.clock       = clock;
        this.orderAccess = orderAccess;
    }

    @Override
    public void handle(Order order) {
        // when a STAT order arrives, escalate recent URGENT orders of same type
        if (order.getPriority() == Priority.STAT) {
            LocalDateTime cutoff = LocalDateTime.now(clock)
                    .minusMinutes(ESCALATION_WINDOW_MINUTES);

            List<Order> pending = orderAccess.listPendingOrders();
            for (Order existing : pending) {
                if (existing.getType() == order.getType()
                        && existing.getPriority() == Priority.URGENT
                        && existing.getTimestamp().isAfter(cutoff)) {

                    existing.setPriority(Priority.STAT);
                    System.out.println("[ESCALATION] Order " + existing.getId()
                            + " escalated URGENT → STAT (same type as incoming STAT)");
                }
            }
        }

        wrapped.handle(order);
    }
}