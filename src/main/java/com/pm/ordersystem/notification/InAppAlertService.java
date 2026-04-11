package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InAppAlertService implements NotificationService {

    private final AtomicInteger badgeCount = new AtomicInteger(0);

    @Override
    public void onOrderStatusChanged(Order order, String event) {
        int count = badgeCount.incrementAndGet();
        System.out.println("[IN-APP] Badge: " + count
                + " | event=" + event
                + " | order=" + order.getId());
    }

    @Override
    public void notifyClinician(String clinicianName, Order order, String event) {
        
    }

    @Override
    public void notifyStaff(String staffName, Order order, String event) {

    }

    public int getBadgeCount() {
        return badgeCount.get();
    }

    public void resetBadgeCount() {
        badgeCount.set(0);
    }
}