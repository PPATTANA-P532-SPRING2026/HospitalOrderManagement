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

        List<String> events = new ArrayList<>();
        List<Order>  orders = new ArrayList<>();

        @Override
        public void onOrderStatusChanged(Order order,
                                         String event) {
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

    // ── existing tests ────────────────────────────────────────────────

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
        assertEquals("SUBMITTED", mockNotifier.events.get(0));
        assertEquals("CLAIMED",   mockNotifier.events.get(1));
        assertEquals("COMPLETED", mockNotifier.events.get(2));
    }

    @Test
    void console_notifier_does_not_throw() {
        // Arrange
        ConsoleNotificationService console =
                new ConsoleNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                console.onOrderStatusChanged(
                        testOrder, "SUBMITTED"));
    }

    // ── Week 2 — InAppAlertService tests ─────────────────────────────

    @Test
    void inapp_badge_increments_on_notify() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();

        // Act
        inApp.onOrderStatusChanged(testOrder, "SUBMITTED");

        // Assert
        assertEquals(1, inApp.getBadgeCount());
    }

    @Test
    void inapp_badge_increments_multiple_times() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();

        // Act
        inApp.onOrderStatusChanged(testOrder, "SUBMITTED");
        inApp.onOrderStatusChanged(testOrder, "CLAIMED");
        inApp.onOrderStatusChanged(testOrder, "COMPLETED");

        // Assert
        assertEquals(3, inApp.getBadgeCount());
    }

    @Test
    void inapp_badge_resets_to_zero() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();
        inApp.onOrderStatusChanged(testOrder, "SUBMITTED");
        inApp.onOrderStatusChanged(testOrder, "CLAIMED");
        assertEquals(2, inApp.getBadgeCount());

        // Act
        inApp.resetBadgeCount();

        // Assert
        assertEquals(0, inApp.getBadgeCount());
    }

    @Test
    void inapp_starts_at_zero() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();

        // Assert
        assertEquals(0, inApp.getBadgeCount());
    }

    // ── Week 2 — EmailNotificationService tests ───────────────────────

    @Test
    void email_notifier_does_not_throw() {
        // Arrange
        EmailNotificationService email =
                new EmailNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                email.onOrderStatusChanged(
                        testOrder, "SUBMITTED"));
    }

    @Test
    void email_notifier_handles_all_events() {
        // Arrange
        EmailNotificationService email =
                new EmailNotificationService();

        // Act + Assert — none of these should throw
        assertDoesNotThrow(() -> {
            email.onOrderStatusChanged(testOrder, "SUBMITTED");
            email.onOrderStatusChanged(testOrder, "CLAIMED");
            email.onOrderStatusChanged(testOrder, "COMPLETED");
            email.onOrderStatusChanged(testOrder, "CANCELLED");
        });
    }

    // ── Week 2 — multiple observers fire ─────────────────────────────

    @Test
    void multiple_observers_all_receive_event() {
        // Arrange
        MockNotificationService observer1 =
                new MockNotificationService();
        MockNotificationService observer2 =
                new MockNotificationService();
        InAppAlertService inApp = new InAppAlertService();

        List<NotificationService> observers =
                List.of(observer1, observer2, inApp);

        // Act — simulate notifyObservers()
        observers.forEach(o ->
                o.onOrderStatusChanged(testOrder, "SUBMITTED"));

        // Assert — all observers received event
        assertEquals(1, observer1.events.size());
        assertEquals(1, observer2.events.size());
        assertEquals(1, inApp.getBadgeCount());
        assertEquals("SUBMITTED", observer1.events.get(0));
        assertEquals("SUBMITTED", observer2.events.get(0));
    }

    @Test
    void observer_list_fires_in_order() {
        // Arrange
        List<String> fireOrder = new ArrayList<>();

        NotificationService first = (order, event) ->
                fireOrder.add("first");
        NotificationService second = (order, event) ->
                fireOrder.add("second");
        NotificationService third = (order, event) ->
                fireOrder.add("third");

        List<NotificationService> observers =
                List.of(first, second, third);

        // Act
        observers.forEach(o ->
                o.onOrderStatusChanged(testOrder, "SUBMITTED"));

        // Assert
        assertEquals(List.of("first", "second", "third"),
                fireOrder);
    }
}