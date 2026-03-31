package com.pm.ordersystem.model.order;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;

public class MedicationOrder extends Order {

    public MedicationOrder(String patientName, String clinician,
                           String description, Priority priority) {
        super(OrderType.MEDICATION, patientName, clinician,
                description, priority);
    }
}