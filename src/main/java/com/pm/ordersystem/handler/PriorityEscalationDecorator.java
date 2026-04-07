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
    private final OrderAccess orderAccess;
    private final Clock clock;

    private static final int WINDOW_MINUTES = 5;

    public PriorityEscalationDecorator(OrderHandler wrapped,
                                       OrderAccess orderAccess,
                                       Clock clock) {
        this.wrapped     = wrapped;
        this.orderAccess = orderAccess;
        this.clock       = clock;
    }

    @Override
    public void handle(Order order) {

        // if incoming order is STAT —
        // escalate recent URGENT orders of same type
        if (order.getPriority() == Priority.STAT) {
            escalateRecentUrgentOrders(order);
        }

        // if incoming order is URGENT —
        // check if a recent STAT of same type exists
        // if so upgrade this order to STAT too
        if (order.getPriority() == Priority.URGENT) {
            checkAndEscalate(order);
        }

        wrapped.handle(order);
    }

    // ── escalate existing URGENT orders of same type ──────────────────
    private void escalateRecentUrgentOrders(Order statOrder) {
        LocalDateTime windowStart = LocalDateTime
                .now(clock)
                .minusMinutes(WINDOW_MINUTES);

        List<Order> pending = orderAccess.listPendingOrders();

        for (Order existing : pending) {
            if (existing.getPriority() == Priority.URGENT
                    && existing.getType() == statOrder.getType()
                    && existing.getStatus() == OrderStatus.PENDING
                    && existing.getTimestamp()
                    .isAfter(windowStart)) {

                existing.setPriority(Priority.STAT);
                orderAccess.saveOrder(existing);

                System.out.println(
                        "[ESCALATION] Order "
                                + existing.getId()
                                + " upgraded URGENT → STAT"
                                + " (within " + WINDOW_MINUTES
                                + " min window of STAT order "
                                + statOrder.getId() + ")");
            }
        }
    }

    // ── check if incoming URGENT should be escalated ──────────────────
    private void checkAndEscalate(Order urgentOrder) {
        LocalDateTime windowStart = LocalDateTime
                .now(clock)
                .minusMinutes(WINDOW_MINUTES);

        boolean recentStatExists = orderAccess
                .listAllOrders()
                .stream()
                .anyMatch(o ->
                        o.getPriority() == Priority.STAT
                                && o.getType() == urgentOrder.getType()
                                && o.getTimestamp().isAfter(windowStart));

        if (recentStatExists) {
            urgentOrder.setPriority(Priority.STAT);
            System.out.println(
                    "[ESCALATION] New URGENT order "
                            + urgentOrder.getId()
                            + " upgraded → STAT"
                            + " (recent STAT of same type exists)");
        }
    }
}