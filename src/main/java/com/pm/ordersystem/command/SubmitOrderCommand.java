package com.pm.ordersystem.command;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.engine.TriagingEngine;
import com.pm.ordersystem.handler.OrderHandler;
import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
import com.pm.ordersystem.notification.NotificationService;

import java.util.List;

public class SubmitOrderCommand implements Command {


    private final OrderType type;
    private final String patientName;
    private final String clinician;
    private final String description;
    private final Priority priority;


    private final OrderAccess orderAccess;
    private final OrderHandler orderHandler;
    private final TriagingEngine triagingEngine;
    private final List<NotificationService> observers;


    private Order createdOrder;

    public SubmitOrderCommand(OrderType type,
                              String patientName,
                              String clinician,
                              String description,
                              Priority priority,
                              OrderAccess orderAccess,
                              OrderHandler orderHandler,
                              TriagingEngine triagingEngine,
                              List<NotificationService> observers) {
        this.type           = type;
        this.patientName    = patientName;
        this.clinician      = clinician;
        this.description    = description;
        this.priority       = priority;
        this.orderAccess    = orderAccess;
        this.orderHandler   = orderHandler;
        this.triagingEngine = triagingEngine;
        this.observers      = observers;
    }

    @Override
    public void execute() {

        createdOrder = OrderFactory.create(
                type, patientName, clinician,
                description, priority);


        orderHandler.handle(createdOrder);


        List<Order> queue = orderAccess.listPendingOrders();
        triagingEngine.assignPosition(createdOrder, queue);


        orderAccess.saveOrder(createdOrder);

        // 5. notify all observers
        observers.forEach(o ->
                o.onOrderStatusChanged(createdOrder, "SUBMITTED"));
    }

    // ── getters ───────────────────────────────────────────────────────
    public OrderType getType()     { return type; }
    public String getPatientName() { return patientName; }
    public String getClinician()   { return clinician; }
    public String getDescription() { return description; }
    public Priority getPriority()  { return priority; }
    public Order getCreatedOrder() { return createdOrder; }
}