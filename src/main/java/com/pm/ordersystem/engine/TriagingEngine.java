package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TriagingEngine {

    private TriageStrategy strategy;

    public TriagingEngine(TriageStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(TriageStrategy strategy) {
        this.strategy = strategy;
    }

    public String getStrategyName() {
        return strategy.getClass().getSimpleName();
    }

    public TriageStrategy getStrategy() {
        return strategy;
    }

    public List<Order> assignPosition(Order order, List<Order> currentQueue) {
        return strategy.insertIntoQueue(order, currentQueue);
    }

    public List<Order> sortQueue(List<Order> queue) {
        List<Order> copy = new ArrayList<>(queue);
        copy.sort(strategy::compare);
        return copy;
    }

    public int compare(Order a, Order b) {
        return strategy.compare(a, b);
    }
}