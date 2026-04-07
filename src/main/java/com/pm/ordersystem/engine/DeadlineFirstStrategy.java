package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DeadlineFirstStrategy implements TriageStrategy {

    // inject Clock for testability
    private final Clock clock;

    public DeadlineFirstStrategy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<Order> insertIntoQueue(Order order,
                                       List<Order> currentQueue) {
        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);

        // sort by ascending time-to-deadline
        // order closest to deadline goes first
        queue.sort(Comparator.comparing(this::getDeadline));
        return queue;
    }

    // calculate deadline based on order type + priority
    public LocalDateTime getDeadline(Order order) {
        int minutes = deadlineMinutes(
                order.getType(), order.getPriority());
        return order.getTimestamp().plusMinutes(minutes);
    }

    // deadline table from spec
    private int deadlineMinutes(OrderType type,
                                Priority priority) {
        return switch (priority) {
            case STAT -> switch (type) {
                case LAB        -> 30;
                case MEDICATION -> 20;
                case IMAGING    -> 45;
            };
            case URGENT -> switch (type) {
                case LAB        -> 120;
                case MEDICATION -> 60;
                case IMAGING    -> 180;
            };
            case ROUTINE -> switch (type) {
                case LAB        -> 480;
                case MEDICATION -> 240;
                case IMAGING    -> 720;
            };
        };
    }
}