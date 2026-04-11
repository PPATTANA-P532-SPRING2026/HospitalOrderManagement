package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoadBalancingStrategy implements TriageStrategy {

    private final Map<String, Integer> staffLoad = new LinkedHashMap<>();

    public void initStaff(List<String> staffNames) {
        staffNames.forEach(name -> staffLoad.putIfAbsent(name, 0));
    }

    @Override
    public List<Order> insertIntoQueue(Order order, List<Order> currentQueue) {
        String assigned = staffLoad.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (assigned != null) {
            order.setClaimedBy(assigned);
            staffLoad.merge(assigned, 1, Integer::sum);
        }

        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);
        return queue;
    }

    @Override
    public int compare(Order a, Order b) {
        return a.getTimestamp().compareTo(b.getTimestamp());
    }
}