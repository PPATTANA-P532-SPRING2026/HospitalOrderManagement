package com.pm.ordersystem.command;

import java.time.LocalDateTime;

public class CommandLogEntry {

    private final String commandType;
    private final String orderId;
    private final String actor;
    private final LocalDateTime timestamp;

    public CommandLogEntry(String commandType,
                           String orderId,
                           String actor) {
        this.commandType = commandType;
        this.orderId     = orderId;
        this.actor       = actor;
        this.timestamp   = LocalDateTime.now();
    }

    public String getCommandType()      { return commandType; }
    public String getOrderId()          { return orderId; }
    public String getActor()            { return actor; }
    public LocalDateTime getTimestamp() { return timestamp; }
}