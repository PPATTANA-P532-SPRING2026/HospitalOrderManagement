package com.pm.ordersystem.manager;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.command.*;
import com.pm.ordersystem.engine.TriagingEngine;
import com.pm.ordersystem.handler.OrderHandler;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
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

    // ── Notify all observers ──────────────────────────────────────────
    private void notifyObservers(Order order, String event) {
        for (NotificationService observer : observers) {
            observer.onOrderStatusChanged(order, event);
        }
    }

    //  Submit Order
    public Order handle(SubmitOrderCommand cmd) {

        // 1. create correct order subtype via Factory
        Order order = OrderFactory.create(
                cmd.getType(),
                cmd.getPatientName(),
                cmd.getClinician(),
                cmd.getDescription(),
                cmd.getPriority()
        );

        // run through decorator chain

        orderHandler.handle(order);

        //  assign position in queue via triage engine
        List<Order> currentQueue = orderAccess.listPendingOrders();
        triagingEngine.assignPosition(order, currentQueue);

        //  save order to store
        orderAccess.saveOrder(order);

        //  notify all registered observers
        notifyObservers(order, "SUBMITTED");

        //  record command in audit log
        commandLog.record("SUBMIT", order.getId(),
                cmd.getClinician());

        return order;
    }

    //  Claim Order
    public Order handle(ClaimOrderCommand cmd) {

        // 1. find the order
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        // 2. validate it is still pending
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order " + cmd.getOrderId()
                            + " cannot be claimed — status is "
                            + order.getStatus());
        }

        // 3. claim it
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy(cmd.getClaimedBy());
        orderAccess.saveOrder(order);

        // 4. notify all observers
        notifyObservers(order, "CLAIMED");

        // 5. record command
        commandLog.record("CLAIM", order.getId(),
                cmd.getClaimedBy());

        return order;
    }

    //  Complete Order
    public Order handle(CompleteOrderCommand cmd) {

        // 1. find the order
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        // 2. validate it is in progress
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Order " + cmd.getOrderId()
                            + " cannot be completed — status is "
                            + order.getStatus());
        }

        // 3. complete it
        order.setStatus(OrderStatus.COMPLETED);
        orderAccess.saveOrder(order);

        // 4. notify all observers
        notifyObservers(order, "COMPLETED");

        // 5. record command
        commandLog.record("COMPLETE", order.getId(),
                cmd.getActor());

        return order;
    }

    // Cancel Order
    public Order handle(CancelOrderCommand cmd) {

        // 1. find the order
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        // 2. validate it is still pending
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order " + cmd.getOrderId()
                            + " cannot be cancelled — status is "
                            + order.getStatus());
        }

        // 3. cancel it
        order.setStatus(OrderStatus.CANCELLED);
        orderAccess.saveOrder(order);

        // 4. notify all observers
        notifyObservers(order, "CANCELLED");

        // 5. record command
        commandLog.record("CANCEL", order.getId(),
                cmd.getActor());

        return order;
    }

    //  Query methods
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