package com.pm.ordersystem.command;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommandLogEntry {

    private final String id;
    private final String commandType;
    private final String orderId;
    private final String actor;
    private final LocalDateTime timestamp;
    private final Command command;

    public CommandLogEntry(String commandType,
                           String orderId,
                           String actor,
                           Command command) {
        this.id          = UUID.randomUUID().toString();
        this.commandType = commandType;
        this.orderId     = orderId;
        this.actor       = actor;
        this.timestamp   = LocalDateTime.now();
        this.command     = command;
    }

    public String getId()               { return id; }
    public String getCommandType()      { return commandType; }
    public String getOrderId()          { return orderId; }
    public String getActor()            { return actor; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Command getCommand()         { return command; }
}