package com.pm.ordersystem.model.order;

import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Order {

    private final String id;
    private final OrderType type;
    private final String patientName;
    private final String clinician;
    private final String description;
    private Priority priority;
    private OrderStatus status;
    private final LocalDateTime timestamp;
    private String claimedBy;

    protected Order(OrderType type, String patientName,
                    String clinician, String description,
                    Priority priority) {
        this.id          = UUID.randomUUID().toString();
        this.type        = type;
        this.patientName = patientName;
        this.clinician   = clinician;
        this.description = description;
        this.priority    = priority;
        this.status      = OrderStatus.PENDING;
        this.timestamp   = LocalDateTime.now();
        this.claimedBy   = null;
    }

    public String getId()               { return id; }
    public OrderType getType()          { return type; }
    public String getPatientName()      { return patientName; }
    public String getClinician()        { return clinician; }
    public String getDescription()      { return description; }
    public Priority getPriority()       { return priority; }
    public OrderStatus getStatus()      { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getClaimedBy()        { return claimedBy; }

    public void setStatus(OrderStatus status)  { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }
}