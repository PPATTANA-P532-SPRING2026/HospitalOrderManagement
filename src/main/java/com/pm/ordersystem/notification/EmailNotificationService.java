package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationService implements NotificationService {

    @Override
    public void onOrderStatusChanged(Order order, String event) {
        String recipient = deriveEmail(order, event);
        System.out.println("[EMAIL] To: " + recipient);
        System.out.println("[EMAIL] Subject: Order " + event
                + " — " + order.getType() + " for " + order.getPatientName());
        System.out.println("[EMAIL] Body: Order " + order.getId()
                + " is now " + order.getStatus()
                + " | Priority: " + order.getPriority()
                + " | Clinician: " + order.getClinician());
    }

    @Override
    public void notifyClinician(String clinicianName, Order order, String event) {

    }

    @Override
    public void notifyStaff(String staffName, Order order, String event) {

    }

    private String deriveEmail(Order order, String event) {
        if (event.equals("CLAIMED") || event.equals("COMPLETED")) {
            String staff = order.getClaimedBy();
            if (staff != null) {
                return staff.toLowerCase().replace(" ", ".") + "@hospital.com";
            }
        }
        return order.getClinician().toLowerCase().replace(" ", ".") + "@hospital.com";
    }
}