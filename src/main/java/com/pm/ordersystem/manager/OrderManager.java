package com.pm.ordersystem.manager;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.command.*;
import com.pm.ordersystem.engine.LoadBalancingStrategy;
import com.pm.ordersystem.engine.TriagingEngine;
import com.pm.ordersystem.handler.OrderHandler;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
import com.pm.ordersystem.model.staff.StaffMember;
import com.pm.ordersystem.notification.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderManager {

    private final OrderAccess orderAccess;
    private final StaffAccess staffAccess;
    private final TriagingEngine triagingEngine;
    private final OrderHandler orderHandler;
    private final CommandLog commandLog;
    private final List<NotificationService> observers;

    @Autowired
    public OrderManager(OrderAccess orderAccess,
                        StaffAccess staffAccess,
                        TriagingEngine triagingEngine,
                        OrderHandler orderHandler,
                        CommandLog commandLog,
                        List<NotificationService> observers) {
        this.orderAccess = orderAccess;
        this.staffAccess = staffAccess;
        this.triagingEngine = triagingEngine;
        this.orderHandler = orderHandler;
        this.commandLog = commandLog;
        this.observers = new ArrayList<>(observers);
    }



    // ── Observer registration ─────────────────────────────────────────
    public void register(NotificationService observer) {
        observers.add(observer);
    }

    public void clearObservers() {
        observers.clear();
    }

    private void notifyObservers(Order order, String event) {
        for (NotificationService obs : observers) {
            obs.onOrderStatusChanged(order, event);

            switch (event) {
                case "SUBMITTED", "CANCELLED", "UNDO_CANCEL" ->
                        obs.notifyClinician(order.getClinician(), order, event);

                case "CLAIMED", "COMPLETED", "UNDO_CLAIM", "UNDO_COMPLETE" -> {
                    obs.notifyClinician(order.getClinician(), order, event);
                    if (order.getClaimedBy() != null) {
                        obs.notifyStaff(order.getClaimedBy(), order, event);
                    }
                }

                default -> {
                    // no extra role-specific routing
                }
            }
        }
    }

    // ── Submit Order ──────────────────────────────────────────────────
    public Order handle(SubmitOrderCommand cmd) {
        Order order = OrderFactory.create(
                cmd.getType(),
                cmd.getPatientName(),
                cmd.getClinician(),
                cmd.getDescription(),
                cmd.getPriority()
        );

        orderHandler.handle(order);

        if (triagingEngine.getStrategy() instanceof LoadBalancingStrategy) {
            String assignee = findLeastLoadedStaff();
            if (assignee != null) {
                order.setClaimedBy(assignee);
            }
        }

        orderAccess.saveOrder(order);
        notifyObservers(order, "SUBMITTED");
        commandLog.record("SUBMIT", order.getId(), cmd.getClinician(), cmd);
        return order;
    }

    // ── Claim Order ───────────────────────────────────────────────────
    public Order handle(ClaimOrderCommand cmd) {
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order cannot be claimed — status is " + order.getStatus());
        }

        String actor = cmd.getClaimedBy();

        // Auto-assign for load balancing when the UI sends no staff name
        if (actor == null || actor.isBlank()) {
            actor = findLeastLoadedStaff();
        }

        if (actor == null || actor.isBlank()) {
            throw new IllegalStateException("No staff available for assignment");
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy(actor);
        orderAccess.saveOrder(order);

        notifyObservers(order, "CLAIMED");
        commandLog.record("CLAIM", order.getId(), actor, cmd);
        return order;
    }

    // ── Complete Order ────────────────────────────────────────────────
    public Order handle(CompleteOrderCommand cmd) {
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Order cannot be completed — status is " + order.getStatus());
        }

        if (order.getClaimedBy() == null || !order.getClaimedBy().equals(cmd.getActor())) {
            throw new IllegalArgumentException(
                    "Order can only be completed by the claimed staff member");
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderAccess.saveOrder(order);

        notifyObservers(order, "COMPLETED");
        commandLog.record("COMPLETE", order.getId(), cmd.getActor(), cmd);
        return order;
    }

    // ── Cancel Order ──────────────────────────────────────────────────
    public Order handle(CancelOrderCommand cmd) {
        Order order = orderAccess.findOrderById(cmd.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + cmd.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order cannot be cancelled — status is " + order.getStatus());
        }

        if (order.getClinician() == null || !order.getClinician().equals(cmd.getActor())) {
            throw new IllegalArgumentException(
                    "Order can only be cancelled by the ordering clinician");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderAccess.saveOrder(order);

        notifyObservers(order, "CANCELLED");
        commandLog.record("CANCEL", order.getId(), cmd.getActor(), cmd);
        return order;
    }

    // ── Undo last command ─────────────────────────────────────────────
    public void undoLast() {
        CommandLogEntry last = commandLog.getLastEntry()
                .orElseThrow(() -> new IllegalStateException("Nothing to undo"));

        String commandType = last.getCommandType();
        String orderId = last.getOrderId();

        switch (commandType) {
            case "SUBMIT" -> {
                orderAccess.removeOrder(orderId);
            }
            case "CLAIM" -> {
                Order order = orderAccess.findOrderById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Order not found: " + orderId));
                order.setStatus(OrderStatus.PENDING);
                order.setClaimedBy(null);
                orderAccess.saveOrder(order);
                notifyObservers(order, "UNDO_CLAIM");
            }
            case "COMPLETE" -> {
                Order order = orderAccess.findOrderById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Order not found: " + orderId));
                order.setStatus(OrderStatus.IN_PROGRESS);
                orderAccess.saveOrder(order);
                notifyObservers(order, "UNDO_COMPLETE");
            }
            case "CANCEL" -> {
                Order order = orderAccess.findOrderById(orderId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Order not found: " + orderId));
                order.setStatus(OrderStatus.PENDING);
                orderAccess.saveOrder(order);
                notifyObservers(order, "UNDO_CANCEL");
            }
            default -> throw new IllegalStateException(
                    "Cannot undo command type: " + commandType);
        }

        commandLog.removeLastEntry();
    }

    // ── Replay a command by id ────────────────────────────────────────
    public Order replay(String entryId) {
        CommandLogEntry entry = commandLog.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Audit entry not found: " + entryId));

        return switch (entry.getCommandType()) {
            case "SUBMIT" -> handle((SubmitOrderCommand) entry.getCommand());
            case "CANCEL" -> handle((CancelOrderCommand) entry.getCommand());
            case "CLAIM" -> handle((ClaimOrderCommand) entry.getCommand());
            case "COMPLETE" -> handle((CompleteOrderCommand) entry.getCommand());
            default -> throw new IllegalStateException(
                    "Cannot replay: " + entry.getCommandType());
        };
    }

    // ── Query ─────────────────────────────────────────────────────────
    public List<Order> getAllOrders() {
        List<Order> all = orderAccess.listAllOrders();

        all.sort((a, b) -> {
            boolean aPending = a.getStatus() == OrderStatus.PENDING;
            boolean bPending = b.getStatus() == OrderStatus.PENDING;
            if (aPending && bPending) {
                return triagingEngine.compare(a, b);
            }
            if (aPending) {
                return -1;
            }
            if (bPending) {
                return 1;
            }
            return a.getTimestamp().compareTo(b.getTimestamp());
        });

        return all;
    }

    public List<Order> getPendingOrders() {
        List<Order> pending = orderAccess.listPendingOrders();
        pending.sort(triagingEngine::compare);
        return pending;
    }

    public List<Order> getInProgressOrders() {
        return orderAccess.listInProgressOrders();
    }

    public List<CommandLogEntry> getAuditLog() {
        return commandLog.getEntries();
    }

    private String findLeastLoadedStaff() {
        if (staffAccess == null) {
            System.out.println("staffAccess is null");
            return null;
        }

        List<StaffMember> staff = staffAccess.listAllStaff();
        System.out.println("staff count = " + staff.size());
        staff.forEach(s -> System.out.println("staff = " + s.getName()));

        List<Order> inProgress = orderAccess.listInProgressOrders();
        System.out.println("in-progress count = " + inProgress.size());

        return staff.stream()
                .min(Comparator
                        .comparingInt((StaffMember s) ->
                                (int) inProgress.stream()
                                        .filter(o -> s.getName().equals(o.getClaimedBy()))
                                        .count())
                        .thenComparing(StaffMember::getName))
                .map(StaffMember::getName)
                .orElse(null);
    }
}