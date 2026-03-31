package com.pm.ordersystem.command;

public class ClaimOrderCommand implements Command {

    private final String orderId;
    private final String claimedBy;

    public ClaimOrderCommand(String orderId, String claimedBy) {
        this.orderId   = orderId;
        this.claimedBy = claimedBy;
    }

    public String getOrderId()   { return orderId; }
    public String getClaimedBy() { return claimedBy; }

    @Override
    public void execute() {
        // execution delegated to OrderManager
    }
}