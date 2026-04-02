package com.pm.ordersystem.command;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;

public class SubmitOrderCommand implements Command {

    private final OrderType type;
    private final String patientName;
    private final String clinician;
    private final String description;
    private final Priority priority;

    public SubmitOrderCommand(OrderType type,
                              String patientName,
                              String clinician,
                              String description,
                              Priority priority) {
        this.type        = type;
        this.patientName = patientName;
        this.clinician   = clinician;
        this.description = description;
        this.priority    = priority;
    }

    // ── getters ───────────────────────────────────────────────────────
    public OrderType getType()        { return type; }
    public String getPatientName()    { return patientName; }
    public String getClinician()      { return clinician; }
    public String getDescription()    { return description; }
    public Priority getPriority()     { return priority; }

    @Override
    public void execute() {
        // execution delegated to OrderManager

    }
}