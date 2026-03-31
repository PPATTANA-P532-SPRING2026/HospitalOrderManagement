package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

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


    public List<Order> assignPosition(Order order,
                                      List<Order> currentQueue) {
        return strategy.insertIntoQueue(order, currentQueue);
    }
}