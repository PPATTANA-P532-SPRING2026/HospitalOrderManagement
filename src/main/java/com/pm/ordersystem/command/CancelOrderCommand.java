package com.pm.ordersystem.command;

public class CancelOrderCommand implements Command {

    private final String orderId;
    private final String actor;

    public CancelOrderCommand(String orderId, String actor) {
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