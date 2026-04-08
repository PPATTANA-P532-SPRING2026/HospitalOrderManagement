package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConsoleNotificationService
        implements NotificationService {

    @Override
    public void onOrderStatusChanged(Order order, String event) {
        System.out.println("[NOTIFICATION] "
                + LocalDateTime.now()
                + " | event="     + event
                + " | orderId="   + order.getId()
                + " | type="      + order.getType()
                + " | patient="   + order.getPatientName()
                + " | priority="  + order.getPriority()
                + " | status="    + order.getStatus());
    }

    @Override
    public void notifyClinician(String clinicianName,
                                Order order, String event) {
        System.out.println("[NOTIFICATION → CLINICIAN] "
                + LocalDateTime.now()
                + " | to="        + clinicianName
                + " | event="     + event
                + " | orderId="   + order.getId()
                + " | type="      + order.getType()
                + " | patient="   + order.getPatientName()
                + " | priority="  + order.getPriority()
                + " | status="    + order.getStatus());
    }

    @Override
    public void notifyStaff(String staffName,
                            Order order, String event) {
        System.out.println("[NOTIFICATION → STAFF] "
                + LocalDateTime.now()
                + " | to="        + staffName
                + " | event="     + event
                + " | orderId="   + order.getId()
                + " | type="      + order.getType()
                + " | patient="   + order.getPatientName()
                + " | priority="  + order.getPriority()
                + " | status="    + order.getStatus());
    }
}