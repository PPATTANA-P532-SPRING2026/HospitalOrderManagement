package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.order.Order;

public class ValidationDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    public ValidationDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle(Order order) {

        if (order.getPatientName() == null
                || order.getPatientName().isBlank()) {
            throw new IllegalArgumentException(
                    "Patient name cannot be empty");
        }
        if (order.getClinician() == null
                || order.getClinician().isBlank()) {
            throw new IllegalArgumentException(
                    "Clinician cannot be empty");
        }
        if (order.getDescription() == null
                || order.getDescription().isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty");
        }
        if (order.getPriority() == null) {
            throw new IllegalArgumentException(
                    "Priority cannot be null");
        }
        if (order.getType() == null) {
            throw new IllegalArgumentException(
                    "Order type cannot be null");
        }

        System.out.println("[VALIDATION] Order "
                + order.getId() + " passed validation");


        wrapped.handle(order);
    }
}