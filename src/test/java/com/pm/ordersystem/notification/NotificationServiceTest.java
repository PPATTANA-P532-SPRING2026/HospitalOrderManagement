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

    // ── mock that captures all notifications ──────────────────────────
    private static class MockNotificationService
            implements NotificationService {

        List<String> events          = new ArrayList<>();
        List<String> clinicianEvents = new ArrayList<>();
        List<String> staffEvents     = new ArrayList<>();
        List<String> clinicianNames  = new ArrayList<>();
        List<String> staffNames      = new ArrayList<>();
        List<Order>  orders          = new ArrayList<>();

        @Override
        public void onOrderStatusChanged(Order order,
                                         String event) {
            events.add(event);
            orders.add(order);
        }

        @Override
        public void notifyClinician(String clinicianName,
                                    Order order,
                                    String event) {
            clinicianEvents.add(event);
            clinicianNames.add(clinicianName);
            orders.add(order);
        }

        @Override
        public void notifyStaff(String staffName,
                                Order order,
                                String event) {
            staffEvents.add(event);
            staffNames.add(staffName);
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

    // ── onOrderStatusChanged tests ────────────────────────────────────

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

    // ── notifyClinician tests ─────────────────────────────────────────

    @Test
    void notify_clinician_captures_event_and_name() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "SUBMITTED");

        // Assert
        assertEquals(1, mockNotifier.clinicianEvents.size());
        assertEquals("SUBMITTED",
                mockNotifier.clinicianEvents.get(0));
        assertEquals("Dr. Jones",
                mockNotifier.clinicianNames.get(0));
    }

    @Test
    void notify_clinician_on_submitted() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "SUBMITTED");

        // Assert
        assertEquals("SUBMITTED",
                mockNotifier.clinicianEvents.get(0));
        assertEquals("Dr. Jones",
                mockNotifier.clinicianNames.get(0));
    }

    @Test
    void notify_clinician_on_claimed() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "CLAIMED");

        // Assert
        assertEquals("CLAIMED",
                mockNotifier.clinicianEvents.get(0));
    }

    @Test
    void notify_clinician_on_completed() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "COMPLETED");

        // Assert
        assertEquals("COMPLETED",
                mockNotifier.clinicianEvents.get(0));
    }

    @Test
    void notify_clinician_on_cancelled() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "CANCELLED");

        // Assert
        assertEquals("CANCELLED",
                mockNotifier.clinicianEvents.get(0));
    }

    // ── notifyStaff tests ─────────────────────────────────────────────

    @Test
    void notify_staff_captures_event_and_name() {
        // Act
        mockNotifier.notifyStaff("Nurse Williams",
                testOrder, "CLAIMED");

        // Assert
        assertEquals(1, mockNotifier.staffEvents.size());
        assertEquals("CLAIMED",
                mockNotifier.staffEvents.get(0));
        assertEquals("Nurse Williams",
                mockNotifier.staffNames.get(0));
    }

    @Test
    void notify_staff_on_claimed() {
        // Act
        mockNotifier.notifyStaff("Nurse Williams",
                testOrder, "CLAIMED");

        // Assert
        assertEquals("CLAIMED",
                mockNotifier.staffEvents.get(0));
    }

    @Test
    void notify_staff_on_completed() {
        // Act
        mockNotifier.notifyStaff("Nurse Williams",
                testOrder, "COMPLETED");

        // Assert
        assertEquals("COMPLETED",
                mockNotifier.staffEvents.get(0));
    }

    // ── role-based notification routing tests ─────────────────────────

    @Test
    void submit_notifies_clinician_only() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "SUBMITTED");

        // Assert — clinician notified, staff not notified
        assertEquals(1, mockNotifier.clinicianEvents.size());
        assertEquals(0, mockNotifier.staffEvents.size());
        assertEquals("SUBMITTED",
                mockNotifier.clinicianEvents.get(0));
    }

    @Test
    void claim_notifies_both_clinician_and_staff() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "CLAIMED");
        mockNotifier.notifyStaff("Nurse Williams",
                testOrder, "CLAIMED");

        // Assert — both notified
        assertEquals(1, mockNotifier.clinicianEvents.size());
        assertEquals(1, mockNotifier.staffEvents.size());
        assertEquals("CLAIMED",
                mockNotifier.clinicianEvents.get(0));
        assertEquals("CLAIMED",
                mockNotifier.staffEvents.get(0));
        assertEquals("Dr. Jones",
                mockNotifier.clinicianNames.get(0));
        assertEquals("Nurse Williams",
                mockNotifier.staffNames.get(0));
    }

    @Test
    void complete_notifies_both_clinician_and_staff() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "COMPLETED");
        mockNotifier.notifyStaff("Nurse Williams",
                testOrder, "COMPLETED");

        // Assert — both notified
        assertEquals(1, mockNotifier.clinicianEvents.size());
        assertEquals(1, mockNotifier.staffEvents.size());
        assertEquals("COMPLETED",
                mockNotifier.clinicianEvents.get(0));
        assertEquals("COMPLETED",
                mockNotifier.staffEvents.get(0));
    }

    @Test
    void cancel_notifies_clinician_only() {
        // Act
        mockNotifier.notifyClinician("Dr. Jones",
                testOrder, "CANCELLED");

        // Assert — clinician notified, staff not notified
        assertEquals(1, mockNotifier.clinicianEvents.size());
        assertEquals(0, mockNotifier.staffEvents.size());
        assertEquals("CANCELLED",
                mockNotifier.clinicianEvents.get(0));
    }

    // ── console notifier tests ────────────────────────────────────────

    @Test
    void console_notifier_does_not_throw_on_status_change() {
        // Arrange
        ConsoleNotificationService console =
                new ConsoleNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                console.onOrderStatusChanged(
                        testOrder, "SUBMITTED"));
    }

    @Test
    void console_notifier_does_not_throw_on_clinician_notify() {
        // Arrange
        ConsoleNotificationService console =
                new ConsoleNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                console.notifyClinician("Dr. Jones",
                        testOrder, "SUBMITTED"));
    }

    @Test
    void console_notifier_does_not_throw_on_staff_notify() {
        // Arrange
        ConsoleNotificationService console =
                new ConsoleNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                console.notifyStaff("Nurse Williams",
                        testOrder, "CLAIMED"));
    }

    // ── InAppAlertService tests ───────────────────────────────────────

    @Test
    void inapp_badge_increments_on_status_change() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();

        // Act
        inApp.onOrderStatusChanged(testOrder, "SUBMITTED");

        // Assert
        assertEquals(1, inApp.getBadgeCount());
    }

    @Test
    void inapp_badge_increments_on_clinician_notify() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();

        // Act
        inApp.notifyClinician("Dr. Jones",
                testOrder, "SUBMITTED");

        // Assert
        assertEquals(1, inApp.getBadgeCount());
    }

    @Test
    void inapp_badge_increments_on_staff_notify() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();

        // Act
        inApp.notifyStaff("Nurse Williams",
                testOrder, "CLAIMED");

        // Assert
        assertEquals(1, inApp.getBadgeCount());
    }

    @Test
    void inapp_badge_resets_to_zero() {
        // Arrange
        InAppAlertService inApp = new InAppAlertService();
        inApp.onOrderStatusChanged(testOrder, "SUBMITTED");
        inApp.notifyClinician("Dr. Jones",
                testOrder, "CLAIMED");
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

    // ── EmailNotificationService tests ───────────────────────────────

    @Test
    void email_does_not_throw_on_status_change() {
        // Arrange
        EmailNotificationService email =
                new EmailNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                email.onOrderStatusChanged(
                        testOrder, "SUBMITTED"));
    }

    @Test
    void email_does_not_throw_on_clinician_notify() {
        // Arrange
        EmailNotificationService email =
                new EmailNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                email.notifyClinician("Dr. Jones",
                        testOrder, "SUBMITTED"));
    }

    @Test
    void email_does_not_throw_on_staff_notify() {
        // Arrange
        EmailNotificationService email =
                new EmailNotificationService();

        // Act + Assert
        assertDoesNotThrow(() ->
                email.notifyStaff("Nurse Williams",
                        testOrder, "CLAIMED"));
    }

    // ── multiple observers tests ──────────────────────────────────────

    @Test
    void multiple_observers_all_receive_clinician_event() {
        // Arrange
        MockNotificationService observer1 =
                new MockNotificationService();
        MockNotificationService observer2 =
                new MockNotificationService();
        InAppAlertService inApp = new InAppAlertService();

        List<NotificationService> observers =
                List.of(observer1, observer2, inApp);

        // Act
        observers.forEach(o ->
                o.notifyClinician("Dr. Jones",
                        testOrder, "SUBMITTED"));

        // Assert
        assertEquals(1, observer1.clinicianEvents.size());
        assertEquals(1, observer2.clinicianEvents.size());
        assertEquals(1, inApp.getBadgeCount());
        assertEquals("SUBMITTED",
                observer1.clinicianEvents.get(0));
        assertEquals("SUBMITTED",
                observer2.clinicianEvents.get(0));
    }

    @Test
    void multiple_observers_all_receive_staff_event() {
        // Arrange
        MockNotificationService observer1 =
                new MockNotificationService();
        MockNotificationService observer2 =
                new MockNotificationService();

        List<NotificationService> observers =
                List.of(observer1, observer2);

        // Act
        observers.forEach(o ->
                o.notifyStaff("Nurse Williams",
                        testOrder, "CLAIMED"));

        // Assert
        assertEquals(1, observer1.staffEvents.size());
        assertEquals(1, observer2.staffEvents.size());
        assertEquals("CLAIMED", observer1.staffEvents.get(0));
        assertEquals("CLAIMED", observer2.staffEvents.get(0));
    }
}