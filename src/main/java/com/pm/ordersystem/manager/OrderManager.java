package com.pm.ordersystem.manager;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.command.*;
import com.pm.ordersystem.engine.TriagingEngine;
import com.pm.ordersystem.handler.OrderHandler;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderManager {

    private final OrderAccess orderAccess;
    private final TriagingEngine triagingEngine;
    private final OrderHandler orderHandler;
    private final CommandLog commandLog;
    private final List<NotificationService> observers;

    public OrderManager(OrderAccess orderAccess,
                        TriagingEngine triagingEngine,
                        OrderHandler orderHandler,
                        CommandLog commandLog,
                        List<NotificationService> observers) {
        this.orderAccess    = orderAccess;
        this.triagingEngine = triagingEngine;
        this.orderHandler   = orderHandler;
        this.commandLog     = commandLog;
        this.observers      = new ArrayList<>(observers);
    }

    // ── Observer registration ─────────────────────────────────────────
    public void register(NotificationService observer) {
        observers.add(observer);
    }

    public void clearObservers() {
        observers.clear();
    }

    // ── handle — calls execute() and records ──────────────────────────
    public Order handle(SubmitOrderCommand cmd) {
        cmd.execute();
        commandLog.record("SUBMIT",
                cmd.getCreatedOrder().getId(),
                cmd.getClinician());
        return cmd.getCreatedOrder();
    }

    public Order handle(ClaimOrderCommand cmd) {
        cmd.execute();
        Order order = orderAccess
                .findOrderById(cmd.getOrderId())
                .orElseThrow();
        commandLog.record("CLAIM",
                cmd.getOrderId(),
                cmd.getClaimedBy());
        return order;
    }

    public Order handle(CompleteOrderCommand cmd) {
        cmd.execute();
        Order order = orderAccess
                .findOrderById(cmd.getOrderId())
                .orElseThrow();
        commandLog.record("COMPLETE",
                cmd.getOrderId(),
                cmd.getActor());
        return order;
    }

    public Order handle(CancelOrderCommand cmd) {
        cmd.execute();
        Order order = orderAccess
                .findOrderById(cmd.getOrderId())
                .orElseThrow();
        commandLog.record("CANCEL",
                cmd.getOrderId(),
                cmd.getActor());
        return order;
    }

    // ── Query methods ─────────────────────────────────────────────────
    public List<Order> getPendingOrders() {
        return orderAccess.listPendingOrders();
    }

    public List<Order> getInProgressOrders() {
        return orderAccess.listInProgressOrders();
    }

    public List<Order> getAllOrders() {
        return orderAccess.listAllOrders();
    }

    public List<CommandLogEntry> getAuditLog() {
        return commandLog.getEntries();
    }
}