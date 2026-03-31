package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.order.Order;

public interface OrderHandler {
    void handle(Order order);
}