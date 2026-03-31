package com.pm.ordersystem.handler;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DecoratorChainTest {

    private OrderHandler chain;

    @BeforeEach
    void setUp() {
        // Arrange — build chain same as AppConfig
        OrderHandler base = new BaseOrderHandler();
        OrderHandler audit = new AuditLoggingDecorator(base);
        OrderHandler boost = new PriorityBoostDecorator(audit);
        chain = new ValidationDecorator(boost);
    }

    @Test
    void valid_order_passes_through_chain() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act + Assert — no exception thrown
        assertDoesNotThrow(() -> chain.handle(order));
    }

    @Test
    void empty_patient_name_throws_exception() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                chain.handle(order));
    }

    @Test
    void empty_clinician_throws_exception() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "",
                "Blood test", Priority.ROUTINE);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                chain.handle(order));
    }

    @Test
    void empty_description_throws_exception() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "", Priority.ROUTINE);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                chain.handle(order));
    }

    @Test
    void emergency_keyword_boosts_priority_to_stat() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "emergency blood test", Priority.ROUTINE);

        // Act
        chain.handle(order);

        // Assert — priority boosted to STAT
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void stat_order_not_boosted_further() {
        // Arrange — already STAT
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "emergency blood test", Priority.STAT);

        // Act
        chain.handle(order);

        // Assert — stays STAT
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void cardiac_keyword_boosts_priority_to_stat() {
        // Arrange
        Order order = OrderFactory.create(OrderType.IMAGING,
                "John Smith", "Dr. Jones",
                "cardiac scan needed", Priority.URGENT);

        // Act
        chain.handle(order);

        // Assert
        assertEquals(Priority.STAT, order.getPriority());
    }
}