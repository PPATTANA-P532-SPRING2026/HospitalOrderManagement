package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConsoleNotificationService implements NotificationService {

    @Override
    public void notify(Order order, String event) {
        System.out.println("[NOTIFICATION] "
                + LocalDateTime.now()
                + " | event="     + event
                + " | orderId="   + order.getId()
                + " | type="      + order.getType()
                + " | patient="   + order.getPatientName()
                + " | clinician=" + order.getClinician()
                + " | priority="  + order.getPriority()
                + " | status="    + order.getStatus());
    }
}
