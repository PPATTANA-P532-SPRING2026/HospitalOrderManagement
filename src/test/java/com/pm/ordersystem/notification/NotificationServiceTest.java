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

    private static class MockNotificationService implements NotificationService {
        List<String> events = new ArrayList<>();
        @Override
        public void onOrderStatusChanged(Order order, String event) {
            events.add(event);
        }

        @Override
        public void notifyClinician(String clinicianName, Order order, String event) {

        }

        @Override
        public void notifyStaff(String staffName, Order order, String event) {

        }
    }

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = OrderFactory.create(OrderType.LAB, "John",
                "Dr. Jones", "test", Priority.URGENT);
    }

    @Test
    void mock_observer_receives_submitted_event() {
        // Arrange
        MockNotificationService obs = new MockNotificationService();
        // Act
        obs.onOrderStatusChanged(testOrder, "SUBMITTED");
        // Assert
        assertEquals(1, obs.events.size());
        assertEquals("SUBMITTED", obs.events.get(0));
    }

    @Test
    void multiple_observers_all_receive_event() {
        // Arrange
        MockNotificationService obs1 = new MockNotificationService();
        MockNotificationService obs2 = new MockNotificationService();
        // Act
        obs1.onOrderStatusChanged(testOrder, "CLAIMED");
        obs2.onOrderStatusChanged(testOrder, "CLAIMED");
        // Assert
        assertEquals("CLAIMED", obs1.events.get(0));
        assertEquals("CLAIMED", obs2.events.get(0));
    }

    @Test
    void in_app_alert_badge_increments_on_notify() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();
        assertEquals(0, inApp.getBadgeCount());
        // Act
        inApp.onOrderStatusChanged(testOrder, "SUBMITTED");
        inApp.onOrderStatusChanged(testOrder, "CLAIMED");
        // Assert
        assertEquals(2, inApp.getBadgeCount());
    }

    @Test
    void in_app_alert_badge_resets() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();
        inApp.onOrderStatusChanged(testOrder, "SUBMITTED");
        // Act
        inApp.resetBadgeCount();
        // Assert
        assertEquals(0, inApp.getBadgeCount());
    }

    @Test
    void console_service_does_not_throw() {
        // Arrange
        ConsoleNotificationService console = new ConsoleNotificationService();
        // Act + Assert
        assertDoesNotThrow(() ->
                console.onOrderStatusChanged(testOrder, "SUBMITTED"));
    }

    @Test
    void email_service_does_not_throw() {
        // Arrange
        EmailNotificationService email = new EmailNotificationService();
        // Act + Assert
        assertDoesNotThrow(() ->
                email.onOrderStatusChanged(testOrder, "SUBMITTED"));
    }
}