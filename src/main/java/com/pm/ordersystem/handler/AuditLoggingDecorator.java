package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.order.Order;

import java.time.LocalDateTime;

public class AuditLoggingDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    public AuditLoggingDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle(Order order) {
        // log before passing down chain
        System.out.println("[AUDIT] "
                + LocalDateTime.now()
                + " | Order: " + order.getId()
                + " | Type: "     + order.getType()
                + " | Patient: "  + order.getPatientName()
                + " | Priority: " + order.getPriority()
                + " | Clinician: "+ order.getClinician());


        wrapped.handle(order);
    }
}
