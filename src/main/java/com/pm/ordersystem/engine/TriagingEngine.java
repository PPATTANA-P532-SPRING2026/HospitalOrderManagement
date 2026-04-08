package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TriagingEngine {

    private TriageStrategy strategy;

    // @Qualifier ensures Spring picks the @Primary bean
    public TriagingEngine(
            @Qualifier("triageStrategy") TriageStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(TriageStrategy strategy) {
        this.strategy = strategy;
    }

    public TriageStrategy getStrategy() {
        return strategy;
    }

    public String getStrategyName() {
        return strategy.getClass().getSimpleName();
    }

    public List<Order> assignPosition(Order order,
                                      List<Order> currentQueue) {
        return strategy.insertIntoQueue(order, currentQueue);
    }
}