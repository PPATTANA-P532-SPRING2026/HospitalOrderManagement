package com.pm.ordersystem.resource;

import com.pm.ordersystem.model.order.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class OrderStore {

    // raw in-memory storage — just a list
    private final List<Order> orders = new ArrayList<>();

    // ── raw operations — no business logic ───────────────────────────
    public void add(Order order) {
        orders.add(order);
    }

    public void remove(Order order) {
        orders.remove(order);
    }

    public List<Order> getAll() {
        return Collections.unmodifiableList(orders);
    }
}