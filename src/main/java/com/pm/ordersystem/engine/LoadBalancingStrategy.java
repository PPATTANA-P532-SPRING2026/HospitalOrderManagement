package com.pm.ordersystem.engine;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.staff.StaffMember;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // if no staff registered fall back to simple queue
        if (allStaff.isEmpty()) {
            List<Order> queue = new ArrayList<>(currentQueue);
            queue.add(order);
            return queue;
        }

        // count IN_PROGRESS orders per staff member
        Map<String, Long> loadMap = orderAccess
                .listInProgressOrders()
                .stream()
                .filter(o -> o.getClaimedBy() != null)
                .collect(Collectors.groupingBy(
                        Order::getClaimedBy,
                        Collectors.counting()));

        // find staff member with fewest in-progress orders
        // start all staff at 0 so new staff are considered
        StaffMember leastLoaded = allStaff.stream()
                .min((a, b) -> {
                    long loadA = loadMap.getOrDefault(
                            a.getName(), 0L);
                    long loadB = loadMap.getOrDefault(
                            b.getName(), 0L);
                    return Long.compare(loadA, loadB);
                })
                .orElse(null);

        // assign to least loaded staff member
        if (leastLoaded != null) {
            order.setClaimedBy(leastLoaded.getName());
        }

        // add to queue
        List<Order> queue = new ArrayList<>(currentQueue);
        queue.add(order);
        return queue;
    }
}