package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;

import java.time.LocalDateTime;

public class StatAuditDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    public StatAuditDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle(Order order) {

        // extra detail logging for STAT orders only
        if (order.getPriority() == Priority.STAT) {
            System.out.println(
                    "[STAT-AUDIT] ─────────────────────────");
            System.out.println(
                    "[STAT-AUDIT] STAT order detected");
            System.out.println(
                    "[STAT-AUDIT] Time:      "
                            + LocalDateTime.now());
            System.out.println(
                    "[STAT-AUDIT] Order ID:  "
                            + order.getId());
            System.out.println(
                    "[STAT-AUDIT] Type:      "
                            + order.getType());
            System.out.println(
                    "[STAT-AUDIT] Patient:   "
                            + order.getPatientName());
            System.out.println(
                    "[STAT-AUDIT] Clinician: "
                            + order.getClinician());
            System.out.println(
                    "[STAT-AUDIT] Description: "
                            + order.getDescription());
            System.out.println(
                    "[STAT-AUDIT] ─────────────────────────");
        }

        wrapped.handle(order);
    }
}