package com.pm.ordersystem.handler;

import com.pm.ordersystem.command.CommandLog;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;

public class StatAuditDecorator implements OrderHandler {

    private final OrderHandler wrapped;
    private final CommandLog commandLog;

    public StatAuditDecorator(OrderHandler wrapped, CommandLog commandLog) {
        this.wrapped = wrapped;
        this.commandLog = commandLog;
    }

    @Override
    public void handle(Order order) {
        if (order.getPriority() == Priority.STAT) {
            String detail = "STAT audit | patient=" + order.getPatientName()
                    + " | type=" + order.getType()
                    + " | clinician=" + order.getClinician()
                    + " | escalationDecision=STAT";

            System.out.println("[STAT AUDIT] " + detail);
        }

        wrapped.handle(order);
    }
}