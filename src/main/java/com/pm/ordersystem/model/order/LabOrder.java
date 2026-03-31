package com.pm.ordersystem.model.order;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;

public class LabOrder extends Order {

    public LabOrder(String patientName, String clinician,
                    String description, Priority priority) {
        super(OrderType.LAB, patientName, clinician,
                description, priority);
    }
}