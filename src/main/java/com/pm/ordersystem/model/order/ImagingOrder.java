package com.pm.ordersystem.model.order;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;

public class ImagingOrder extends Order {

    public ImagingOrder(String patientName, String clinician,
                        String description, Priority priority) {
        super(OrderType.IMAGING, patientName, clinician,
                description, priority);
    }
}