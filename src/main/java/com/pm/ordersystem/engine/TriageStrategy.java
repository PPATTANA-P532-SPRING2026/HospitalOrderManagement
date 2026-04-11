package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;

import java.util.Comparator;
import java.util.List;

public interface TriageStrategy {
    List<Order> insertIntoQueue(Order order, List<Order> currentQueue);

    int compare(Order a, Order b);

    default Comparator<Order> comparator() {
        return this::compare;
    }
}