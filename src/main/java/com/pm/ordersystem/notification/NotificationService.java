package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.order.Order;

public interface NotificationService {
    void notify(Order order, String event);
}