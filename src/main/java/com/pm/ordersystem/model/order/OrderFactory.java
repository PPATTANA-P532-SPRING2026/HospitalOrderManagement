package com.pm.ordersystem.model.order;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;

public class OrderFactory {

    public static Order create(OrderType type,
                               String patientName,
                               String clinician,
                               String description,
                               Priority priority) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "OrderType cannot be null");
        }

        return switch (type) {
            case LAB        -> new LabOrder(
                    patientName, clinician,
                    description, priority);
            case MEDICATION -> new MedicationOrder(
                    patientName, clinician,
                    description, priority);
            case IMAGING    -> new ImagingOrder(
                    patientName, clinician,
                    description, priority);
        };
    }
}
