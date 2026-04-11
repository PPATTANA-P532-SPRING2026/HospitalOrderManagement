package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DeadlineFirstStrategy implements TriageStrategy {

    private final Clock clock;

    public DeadlineFirstStrategy(Clock clock) {
        this.clock = clock;
    }

    public long getDeadlineMinutes(Order order) {
        return switch (order.getType()) {
            case LAB -> switch (order.getPriority()) {
                case STAT -> 30;
                case URGENT -> 120;
                case ROUTINE -> 480;
            };
            case MEDICATION -> switch (order.getPriority()) {
                case STAT -> 15;
                case URGENT -> 60;
                case ROUTINE -> 240;
            };
            case IMAGING -> switch (order.getPriority()) {
                case STAT -> 45;
                case URGENT -> 180;
                case ROUTINE -> 720;
            };
        };
    }

    public long minutesRemaining(Order order) {
        LocalDateTime deadline = order.getTimestamp().plusMinutes(getDeadlineMinutes(order));
        LocalDateTime now = LocalDateTime.now(clock);
        return Duration.between(now, deadline).toMinutes();
    }

    @Override
    public List<Order> insertIntoQueue(Order order, List<Order> currentQueue) {
        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);
        queue.sort(Comparator.comparingLong(this::minutesRemaining)
                .thenComparing(Order::getTimestamp));
        return queue;
    }

    @Override
    public int compare(Order a, Order b) {
        int cmp = Long.compare(minutesRemaining(a), minutesRemaining(b));
        if (cmp != 0) {
            return cmp;
        }
        return a.getTimestamp().compareTo(b.getTimestamp());
    }
}