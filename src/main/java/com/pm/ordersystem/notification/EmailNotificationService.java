package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmailNotificationService
        implements NotificationService {

    @Override
    public void onOrderStatusChanged(Order order, String event) {
        printEmail("staff@hospital.com", order, event);
    }

    @Override
    public void notifyClinician(String clinicianName,
                                Order order, String event) {
        // mock clinician email address
        String email = clinicianName.toLowerCase()
                .replace(" ", ".") + "@hospital.com";
        printEmail(email, order, event);
    }

    @Override
    public void notifyStaff(String staffName,
                            Order order, String event) {
        // mock staff email address
        String email = staffName.toLowerCase()
                .replace(" ", ".") + "@hospital.com";
        printEmail(email, order, event);
    }

    private void printEmail(String to,
                            Order order, String event) {
        System.out.println("[EMAIL] ──────────────────────────");
        System.out.println("[EMAIL] To:      " + to);
        System.out.println("[EMAIL] Subject: Order " + event
                + " - " + order.getType()
                + " for " + order.getPatientName());
        System.out.println("[EMAIL] Body:");
        System.out.println("[EMAIL]   Event:    " + event);
        System.out.println("[EMAIL]   Order:    " + order.getId());
        System.out.println("[EMAIL]   Patient:  "
                + order.getPatientName());
        System.out.println("[EMAIL]   Priority: "
                + order.getPriority());
        System.out.println("[EMAIL]   Status:   "
                + order.getStatus());
        System.out.println("[EMAIL]   Time:     "
                + LocalDateTime.now());
        System.out.println("[EMAIL] ──────────────────────────");
    }
}