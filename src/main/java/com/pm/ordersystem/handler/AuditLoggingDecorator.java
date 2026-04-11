package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.order.Order;

public class AuditLoggingDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    public AuditLoggingDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle(Order order) {
        System.out.println("[AUDIT LOG] order=" + order.getId()
                + " | type=" + order.getType()
                + " | patient=" + order.getPatientName()
                + " | clinician=" + order.getClinician()
                + " | priority=" + order.getPriority()
                + " | status=" + order.getStatus());

        wrapped.handle(order);
    }
}