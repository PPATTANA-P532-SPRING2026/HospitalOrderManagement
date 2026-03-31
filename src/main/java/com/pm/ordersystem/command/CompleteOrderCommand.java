package com.pm.ordersystem.command;

public class CompleteOrderCommand implements Command {

    private final String orderId;
    private final String actor;

    public CompleteOrderCommand(String orderId, String actor) {
        this.orderId = orderId;
        this.actor   = actor;
    }

    public String getOrderId() { return orderId; }
    public String getActor()   { return actor; }

    @Override
    public void execute() {
        // execution delegated to OrderManager
    }
}