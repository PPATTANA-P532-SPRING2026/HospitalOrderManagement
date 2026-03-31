package com.pm.ordersystem.notification;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    // mock that captures notifications
    private static class MockNotificationService
            implements NotificationService {
        List<String> events = new ArrayList<>();
        List<Order> orders  = new ArrayList<>();

        @Override
        public void notify(Order order, String event) {
            events.add(event);
            orders.add(order);
        }
    }

    private MockNotificationService mockService;

    @BeforeEach
    void setUp() {
        // Arrange
        mockService = new MockNotificationService();
    }

    @Test
    void notify_captures_event() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act
        mockService.notify(order, "SUBMITTED");

        // Assert
        assertEquals(1, mockService.events.size());
        assertEquals("SUBMITTED", mockService.events.get(0));
    }

    @Test
    void notify_captures_order() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act
        mockService.notify(order, "CLAIMED");

        // Assert
        assertEquals(order.getId(),
                mockService.orders.get(0).getId());
    }

    @Test
    void console_notifier_does_not_throw() {
        // Arrange
        ConsoleNotificationService service =
                new ConsoleNotificationService();
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act + Assert
        assertDoesNotThrow(() ->
                service.notify(order, "SUBMITTED"));
    }

    @Test
    void notify_called_for_each_event() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Act
        mockService.notify(order, "SUBMITTED");
        mockService.notify(order, "CLAIMED");
        mockService.notify(order, "COMPLETED");

        // Assert
        assertEquals(3, mockService.events.size());
        assertEquals("SUBMITTED", mockService.events.get(0));
        assertEquals("CLAIMED",   mockService.events.get(1));
        assertEquals("COMPLETED", mockService.events.get(2));
    }
}