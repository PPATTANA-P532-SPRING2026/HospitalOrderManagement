package com.pm.ordersystem.engine;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriageStrategyTest {

    private PriorityFirstStrategy strategy;

    @BeforeEach
    void setUp() {
        // Arrange
        strategy = new PriorityFirstStrategy();
    }

    @Test
    void stat_order_goes_to_front_of_queue() {
        // Arrange
        Order routine = OrderFactory.create(OrderType.LAB,
                "Patient A", "Dr. Jones",
                "Blood test", Priority.ROUTINE);
        Order urgent = OrderFactory.create(OrderType.LAB,
                "Patient B", "Dr. Jones",
                "Blood test", Priority.URGENT);
        List<Order> queue = new ArrayList<>(List.of(routine, urgent));

        Order stat = OrderFactory.create(OrderType.LAB,
                "Patient C", "Dr. Jones",
                "Blood test", Priority.STAT);

        // Act
        List<Order> result = strategy.insertIntoQueue(stat, queue);

        // Assert
        assertEquals(stat.getId(), result.get(0).getId());
    }

    @Test
    void routine_order_goes_to_back_of_queue() {
        // Arrange
        Order stat = OrderFactory.create(OrderType.LAB,
                "Patient A", "Dr. Jones",
                "Blood test", Priority.STAT);
        Order urgent = OrderFactory.create(OrderType.LAB,
                "Patient B", "Dr. Jones",
                "Blood test", Priority.URGENT);
        List<Order> queue = new ArrayList<>(List.of(stat, urgent));

        Order routine = OrderFactory.create(OrderType.LAB,
                "Patient C", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act
        List<Order> result = strategy.insertIntoQueue(routine, queue);

        // Assert
        assertEquals(routine.getId(),
                result.get(result.size() - 1).getId());
    }

    @Test
    void same_priority_ordered_by_timestamp_fifo() throws InterruptedException {
        // Arrange
        Order first = OrderFactory.create(OrderType.LAB,
                "Patient A", "Dr. Jones",
                "Blood test", Priority.URGENT);

        Thread.sleep(10); // ensure different timestamp

        Order second = OrderFactory.create(OrderType.LAB,
                "Patient B", "Dr. Jones",
                "Blood test", Priority.URGENT);

        List<Order> queue = new ArrayList<>();

        // Act — insert second first then first
        List<Order> result = strategy.insertIntoQueue(second, queue);
        result = strategy.insertIntoQueue(first, result);

        // Assert — first submitted comes first (FIFO)
        assertEquals(first.getId(), result.get(0).getId());
    }

    @Test
    void empty_queue_returns_single_order() {
        // Arrange
        List<Order> emptyQueue = new ArrayList<>();
        Order order = OrderFactory.create(OrderType.LAB,
                "Patient A", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act
        List<Order> result = strategy.insertIntoQueue(
                order, emptyQueue);

        // Assert
        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).getId());
    }

    @Test
    void priority_order_is_stat_urgent_routine() {
        // Arrange
        Order routine = OrderFactory.create(OrderType.LAB,
                "Patient A", "Dr. Jones",
                "Blood test", Priority.ROUTINE);
        Order urgent = OrderFactory.create(OrderType.LAB,
                "Patient B", "Dr. Jones",
                "Blood test", Priority.URGENT);
        Order stat = OrderFactory.create(OrderType.LAB,
                "Patient C", "Dr. Jones",
                "Blood test", Priority.STAT);

        List<Order> queue = new ArrayList<>();

        // Act — insert in wrong order
        List<Order> result = strategy.insertIntoQueue(routine, queue);
        result = strategy.insertIntoQueue(urgent, result);
        result = strategy.insertIntoQueue(stat, result);

        // Assert — sorted correctly
        assertEquals(Priority.STAT,    result.get(0).getPriority());
        assertEquals(Priority.URGENT,  result.get(1).getPriority());
        assertEquals(Priority.ROUTINE, result.get(2).getPriority());
    }
}