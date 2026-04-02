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

    // ── mock that captures messages ───────────────────────────────────
    private static class MockNotificationService
            implements NotificationService {

        List<String> events  = new ArrayList<>();
        List<Order>  orders  = new ArrayList<>();

        @Override
        public void onOrderStatusChanged(Order order, String event) {
            events.add(event);
            orders.add(order);
        }
    }

    private MockNotificationService mockNotifier;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Arrange
        mockNotifier = new MockNotificationService();
        testOrder    = OrderFactory.create(
                OrderType.LAB,
                "John Smith",
                "Dr. Jones",
                "Blood glucose test",
                Priority.URGENT
        );
    }

    @Test
    void notify_captures_submitted_event() {
        // Act
        mockNotifier.onOrderStatusChanged(testOrder, "SUBMITTED");

        // Assert
        assertEquals(1, mockNotifier.events.size());
        assertEquals("SUBMITTED", mockNotifier.events.get(0));
        assertEquals(testOrder, mockNotifier.orders.get(0));
    }

    @Test
    void notify_captures_claimed_event() {
        // Act
        mockNotifier.onOrderStatusChanged(testOrder, "CLAIMED");

        // Assert
        assertEquals("CLAIMED", mockNotifier.events.get(0));
    }

    @Test
    void notify_captures_completed_event() {
        // Act
        mockNotifier.onOrderStatusChanged(testOrder, "COMPLETED");

        // Assert
        assertEquals("COMPLETED", mockNotifier.events.get(0));
    }

    @Test
    void notify_captures_cancelled_event() {
        // Act
        mockNotifier.onOrderStatusChanged(testOrder, "CANCELLED");

        // Assert
        assertEquals("CANCELLED", mockNotifier.events.get(0));
    }

    @Test
    void notify_captures_multiple_events() {
        // Act
        mockNotifier.onOrderStatusChanged(testOrder, "SUBMITTED");
        mockNotifier.onOrderStatusChanged(testOrder, "CLAIMED");
        mockNotifier.onOrderStatusChanged(testOrder, "COMPLETED");

        // Assert
        assertEquals(3, mockNotifier.events.size());
        assertEquals("SUBMITTED",  mockNotifier.events.get(0));
        assertEquals("CLAIMED",    mockNotifier.events.get(1));
        assertEquals("COMPLETED",  mockNotifier.events.get(2));
    }

    @Test
    void console_notifier_does_not_throw() {
        // Arrange
        ConsoleNotificationService console =
                new ConsoleNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                console.onOrderStatusChanged(testOrder, "SUBMITTED"));
    }

    @Test
    void multiple_observers_all_receive_event() {
        // Arrange
        MockNotificationService observer1 = new MockNotificationService();
        MockNotificationService observer2 = new MockNotificationService();

        // Act
        observer1.onOrderStatusChanged(testOrder, "SUBMITTED");
        observer2.onOrderStatusChanged(testOrder, "SUBMITTED");

        // Assert — both observers received the event
        assertEquals(1, observer1.events.size());
        assertEquals(1, observer2.events.size());
        assertEquals("SUBMITTED", observer1.events.get(0));
        assertEquals("SUBMITTED", observer2.events.get(0));
    }
}