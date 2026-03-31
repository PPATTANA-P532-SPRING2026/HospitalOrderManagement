package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;

import java.util.List;

public interface TriageStrategy {

    // inserts order into the queue at the correct position
    // returns the sorted queue after insertion
    List<Order> insertIntoQueue(Order order, List<Order> currentQueue);
}