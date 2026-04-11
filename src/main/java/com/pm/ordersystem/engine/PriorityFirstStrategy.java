package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Primary
public class PriorityFirstStrategy implements TriageStrategy {

    @Override
    public List<Order> insertIntoQueue(Order order, List<Order> currentQueue) {
        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);
        queue.sort(Comparator.comparingInt(this::priorityValue)
                .reversed()
                .thenComparing(Order::getTimestamp));
        return queue;
    }

    @Override
    public int compare(Order a, Order b) {
        int cmp = Integer.compare(priorityValue(b), priorityValue(a));
        if (cmp != 0) {
            return cmp;
        }
        return a.getTimestamp().compareTo(b.getTimestamp());
    }

    private int priorityValue(Order order) {
        return switch (order.getPriority()) {
            case STAT -> 2;
            case URGENT -> 1;
            case ROUTINE -> 0;
        };
    }
}