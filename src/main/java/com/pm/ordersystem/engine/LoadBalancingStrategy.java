package com.pm.ordersystem.engine;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.staff.StaffMember;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoadBalancingStrategy implements TriageStrategy {

    private final OrderAccess orderAccess;
    private final StaffAccess staffAccess;

    public LoadBalancingStrategy(OrderAccess orderAccess,
                                 StaffAccess staffAccess) {
        this.orderAccess = orderAccess;
        this.staffAccess = staffAccess;
    }

    @Override
    public List<Order> insertIntoQueue(Order order,
                                       List<Order> currentQueue) {
        List<StaffMember> allStaff = staffAccess.listAllStaff();

        if (!allStaff.isEmpty()) {
            Map<String, Long> loadMap = new HashMap<>();

            // initialise all staff at 0
            allStaff.forEach(s ->
                    loadMap.put(s.getName(), 0L));

            // count IN_PROGRESS orders only (per spec)
            orderAccess.listInProgressOrders()
                    .stream()
                    .filter(o -> o.getClaimedBy() != null)
                    .forEach(o -> loadMap.merge(
                            o.getClaimedBy(), 1L, Long::sum));

            // find least loaded
            StaffMember leastLoaded = allStaff.stream()
                    .min((a, b) -> Long.compare(
                            loadMap.get(a.getName()),
                            loadMap.get(b.getName())))
                    .orElse(null);

            if (leastLoaded != null) {
                order.setClaimedBy(leastLoaded.getName());
                System.out.println("[LOAD BALANCING] Assigned "
                        + order.getId()
                        + " → " + leastLoaded.getName());
            }
        }

        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);
        return queue;
    }

    public void rebalance(List<Order> pendingOrders) {
        List<StaffMember> allStaff = staffAccess.listAllStaff();
        if (allStaff.isEmpty()) return;

        // reset all pending assignments
        pendingOrders.forEach(o -> o.setClaimedBy(null));

        // reassign each order one by one
        for (Order o : pendingOrders) {
            Map<String, Long> loadMap = new HashMap<>();

            // initialise all at 0
            allStaff.forEach(s ->
                    loadMap.put(s.getName(), 0L));

            // count IN_PROGRESS
            orderAccess.listInProgressOrders()
                    .stream()
                    .filter(p -> p.getClaimedBy() != null)
                    .forEach(p -> loadMap.merge(
                            p.getClaimedBy(), 1L, Long::sum));

            // count already assigned in this batch
            pendingOrders.stream()
                    .filter(p -> p.getClaimedBy() != null)
                    .forEach(p -> loadMap.merge(
                            p.getClaimedBy(), 1L, Long::sum));

            StaffMember leastLoaded = allStaff.stream()
                    .min((a, b) -> Long.compare(
                            loadMap.get(a.getName()),
                            loadMap.get(b.getName())))
                    .orElse(null);

            if (leastLoaded != null) {
                o.setClaimedBy(leastLoaded.getName());
                orderAccess.saveOrder(o);
                System.out.println("[LOAD BALANCING] Rebalanced "
                        + o.getId()
                        + " → " + leastLoaded.getName());
            }
        }
    }
}