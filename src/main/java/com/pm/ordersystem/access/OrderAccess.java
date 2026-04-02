package com.pm.ordersystem.access;

import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.resource.OrderStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderAccess {

    private final OrderStore orderStore;

    public OrderAccess(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    //  business verbs

    public void saveOrder(Order order) {
        // if order already exists update it in place
        // if not add it
        boolean exists = orderStore.getAll()
                .stream()
                .anyMatch(o -> o.getId().equals(order.getId()));
        if (!exists) {
            orderStore.add(order);
        }
        // if it exists it is already in the list
        // since Order is mutable, changes are reflected automatically
    }

    public Optional<Order> findOrderById(String id) {
        return orderStore.getAll()
                .stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
    }

    public List<Order> listPendingOrders() {
        return orderStore.getAll()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .collect(Collectors.toList());
    }

    public List<Order> listInProgressOrders() {
        return orderStore.getAll()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.IN_PROGRESS)
                .collect(Collectors.toList());
    }

    public List<Order> listAllOrders() {
        return new ArrayList<>(orderStore.getAll());
    }

    public void removeOrder(String id) {
        orderStore.getAll()
                .stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .ifPresent(orderStore::remove);
    }
}
