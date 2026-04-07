package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class PriorityFirstStrategy implements TriageStrategy {

    @Override
    public List<Order> insertIntoQueue(Order order,
                                       List<Order> currentQueue) {
        // add the new order to the queue
        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);

        // sort by priority first then by timestamp for ties
        queue.sort(Comparator
                .comparingInt(this::priorityValue).reversed()
                .thenComparing(Order::getTimestamp));

        return queue;
    }

    // ── STAT = 2, URGENT = 1, ROUTINE = 0
    private int priorityValue(Order order) {
        return switch (order.getPriority()) {
            case STAT    -> 2;
            case URGENT  -> 1;
            case ROUTINE -> 0;
        };
    }
}