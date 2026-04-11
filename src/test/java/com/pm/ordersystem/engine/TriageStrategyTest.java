package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriageStrategyTest {

    // ── PriorityFirstStrategy ─────────────────────────────────────────

    @Test
    void priority_first_stat_before_urgent_before_routine() {
        // Arrange
        PriorityFirstStrategy strategy = new PriorityFirstStrategy();
        Order routine = OrderFactory.create(OrderType.LAB, "P1", "Dr A", "desc", Priority.ROUTINE);
        Order urgent  = OrderFactory.create(OrderType.LAB, "P2", "Dr A", "desc", Priority.URGENT);
        Order stat    = OrderFactory.create(OrderType.LAB, "P3", "Dr A", "desc", Priority.STAT);

        // Act
        List<Order> queue = strategy.insertIntoQueue(routine, new ArrayList<>());
        queue = strategy.insertIntoQueue(urgent, queue);
        queue = strategy.insertIntoQueue(stat, queue);

        // Assert
        assertEquals(Priority.STAT,    queue.get(0).getPriority());
        assertEquals(Priority.URGENT,  queue.get(1).getPriority());
        assertEquals(Priority.ROUTINE, queue.get(2).getPriority());
    }

    @Test
    void priority_first_ties_broken_by_fifo() {
        // Arrange
        PriorityFirstStrategy strategy = new PriorityFirstStrategy();
        Order first  = OrderFactory.create(OrderType.LAB, "P1", "Dr A", "desc", Priority.URGENT);
        Order second = OrderFactory.create(OrderType.LAB, "P2", "Dr A", "desc", Priority.URGENT);

        // Act
        List<Order> queue = strategy.insertIntoQueue(first, new ArrayList<>());
        queue = strategy.insertIntoQueue(second, queue);

        // Assert — first submitted comes first
        assertEquals(first.getId(), queue.get(0).getId());
    }

    // ── LoadBalancingStrategy ─────────────────────────────────────────

    @Test
    void load_balancing_assigns_to_least_loaded_staff() {
        // Arrange
        LoadBalancingStrategy strategy = new LoadBalancingStrategy();
        strategy.initStaff(List.of("Nurse A", "Nurse B"));

        // Act
        Order order1 = OrderFactory.create(OrderType.LAB, "P1", "Dr A", "d", Priority.ROUTINE);
        strategy.insertIntoQueue(order1, new ArrayList<>());

        Order order2 = OrderFactory.create(OrderType.LAB, "P2", "Dr A", "d", Priority.ROUTINE);
        strategy.insertIntoQueue(order2, List.of(order1));

        // Assert — both nurses got one order each
        assertNotNull(order1.getClaimedBy());
        assertNotNull(order2.getClaimedBy());
        assertNotEquals(order1.getClaimedBy(), order2.getClaimedBy());
    }

    // ── DeadlineFirstStrategy ─────────────────────────────────────────

    @Test
    void deadline_first_stat_lab_before_routine_lab() {
        // Arrange
        Clock fixed = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        DeadlineFirstStrategy strategy = new DeadlineFirstStrategy(fixed);

        Order routine = OrderFactory.create(OrderType.LAB, "P1", "Dr A", "d", Priority.ROUTINE);
        Order stat    = OrderFactory.create(OrderType.LAB, "P2", "Dr A", "d", Priority.STAT);

        // Act
        List<Order> queue = strategy.insertIntoQueue(routine, new ArrayList<>());
        queue = strategy.insertIntoQueue(stat, queue);

        // Assert — stat has shorter deadline so comes first
        assertEquals(Priority.STAT, queue.get(0).getPriority());
    }

    @Test
    void deadline_first_medication_stat_has_15_min_deadline() {
        // Arrange
        Clock fixed = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        DeadlineFirstStrategy strategy = new DeadlineFirstStrategy(fixed);
        Order order = OrderFactory.create(OrderType.MEDICATION, "P", "Dr", "d", Priority.STAT);

        // Act + Assert
        assertEquals(15L, strategy.getDeadlineMinutes(order));
    }

    @Test
    void deadline_first_imaging_routine_has_720_min_deadline() {
        // Arrange
        Clock fixed = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        DeadlineFirstStrategy strategy = new DeadlineFirstStrategy(fixed);
        Order order = OrderFactory.create(OrderType.IMAGING, "P", "Dr", "d", Priority.ROUTINE);

        // Act + Assert
        assertEquals(720L, strategy.getDeadlineMinutes(order));
    }
}