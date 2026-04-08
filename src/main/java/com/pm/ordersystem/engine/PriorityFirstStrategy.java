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
        // add new order to full queue
        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);

        // sort entire queue:
        // STAT first, URGENT second, ROUTINE last
        // ties broken by timestamp ascending (FIFO)
        queue.sort(Comparator
                .comparingInt(this::priorityValue)
                .reversed()
                .thenComparing(Order::getTimestamp));

        return queue;
    }

    private int priorityValue(Order order) {
        return switch (order.getPriority()) {
            case STAT    -> 2;
            case URGENT  -> 1;
            case ROUTINE -> 0;
        };
    }
}