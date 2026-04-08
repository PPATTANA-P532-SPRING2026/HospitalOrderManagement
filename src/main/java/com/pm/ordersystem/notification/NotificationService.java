package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;

public interface NotificationService {

    // general notification — all observers
    void onOrderStatusChanged(Order order, String event);

    // role-aware notifications
    void notifyClinician(String clinicianName,
                         Order order, String event);

    void notifyStaff(String staffName,
                     Order order, String event);
}