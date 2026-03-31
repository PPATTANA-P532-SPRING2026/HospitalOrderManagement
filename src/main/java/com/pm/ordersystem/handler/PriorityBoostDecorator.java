package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;

public class PriorityBoostDecorator implements OrderHandler {

    private final OrderHandler wrapped;

    private static final String[] URGENT_KEYWORDS = {
            "critical", "emergency", "life-threatening",
            "immediate", "urgent care", "cardiac"
    };

    public PriorityBoostDecorator(OrderHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle(Order order) {
        wrapped.handle(order);

        String desc = order.getDescription().toLowerCase();
        for (String keyword : URGENT_KEYWORDS) {
            if (desc.contains(keyword)) {
                order.setPriority(Priority.STAT);
                System.out.println("[PRIORITY BOOST] Order "
                        + order.getId()
                        + " boosted to STAT"
                        + " — keyword: " + keyword);
                break;
            }
        }
    }
}