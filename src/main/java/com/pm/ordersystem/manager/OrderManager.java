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

import java.util.List;

@Service
public class OrderManager {

    private final OrderAccess orderAccess;
    private final TriagingEngine triagingEngine;
    private final OrderHandler orderHandler;
    private final NotificationService notificationService;
    private final CommandLog commandLog;

    public OrderManager(OrderAccess orderAccess,
                        TriagingEngine triagingEngine,
                        OrderHandler orderHandler,
                        NotificationService notificationService,
                        CommandLog commandLog) {
        this.orderAccess         = orderAccess;
        this.triagingEngine      = triagingEngine;
        this.orderHandler        = orderHandler;
        this.notificationService = notificationService;
        this.commandLog          = commandLog;
    }

    //  Submit Order
    public Order handle(SubmitOrderCommand cmd) {

        // create correct order subtype via Factory
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
        List<Order> sortedQueue  = triagingEngine.assignPosition(
                order, currentQueue);

        // save order to store
        orderAccess.saveOrder(order);

        //  notify relevant parties
        notificationService.notify(order, "SUBMITTED");

        // record command in audit log
        commandLog.record("SUBMIT", order.getId(),
                cmd.getClinician());

        return order;
    }

    //  Claim Order
    public Order handle(ClaimOrderCommand cmd) {

        // find the order
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        // validate it is still pending
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order " + cmd.getOrderId()
                            + " cannot be claimed — status is "
                            + order.getStatus());
        }

        // claim it
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy(cmd.getClaimedBy());
        orderAccess.saveOrder(order);

        // notify
        notificationService.notify(order, "CLAIMED");

        // record command
        commandLog.record("CLAIM", order.getId(),
                cmd.getClaimedBy());

        return order;
    }

    //  Complete Order
    public Order handle(CompleteOrderCommand cmd) {

        // find the order
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        // validate it is in progress
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Order " + cmd.getOrderId()
                            + " cannot be completed — status is "
                            + order.getStatus());
        }

        //  complete it
        order.setStatus(OrderStatus.COMPLETED);
        orderAccess.saveOrder(order);

        // notify
        notificationService.notify(order, "COMPLETED");

        //  record command
        commandLog.record("COMPLETE", order.getId(),
                cmd.getActor());

        return order;
    }

    //  Cancel Order
    public Order handle(CancelOrderCommand cmd) {

        // find the order
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        // validate it is still pending
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order " + cmd.getOrderId()
                            + " cannot be cancelled — status is "
                            + order.getStatus());
        }

        // cancel it
        order.setStatus(OrderStatus.CANCELLED);
        orderAccess.saveOrder(order);

        // notify
        notificationService.notify(order, "CANCELLED");

        // record command
        commandLog.record("CANCEL", order.getId(),
                cmd.getActor());

        return order;
    }

    //Query methods
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