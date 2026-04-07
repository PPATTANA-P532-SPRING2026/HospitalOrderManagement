package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmailNotificationService implements NotificationService {

    @Override
    public void onOrderStatusChanged(Order order, String event) {
        System.out.println("[EMAIL] ──────────────────────────────");
        System.out.println("[EMAIL] To:       staff@hospital.com");
        System.out.println("[EMAIL] Subject:  Order " + event
                + " — " + order.getType()
                + " for " + order.getPatientName());
        System.out.println("[EMAIL] Body:");
        System.out.println("[EMAIL]   Event:     " + event);
        System.out.println("[EMAIL]   Order ID:  " + order.getId());
        System.out.println("[EMAIL]   Type:      " + order.getType());
        System.out.println("[EMAIL]   Patient:   "
                + order.getPatientName());
        System.out.println("[EMAIL]   Clinician: "
                + order.getClinician());
        System.out.println("[EMAIL]   Priority:  "
                + order.getPriority());
        System.out.println("[EMAIL]   Status:    "
                + order.getStatus());
        System.out.println("[EMAIL]   Time:      "
                + LocalDateTime.now());
        System.out.println("[EMAIL] ──────────────────────────────");
    }
}