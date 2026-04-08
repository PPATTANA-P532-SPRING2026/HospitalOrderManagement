package com.pm.ordersystem.manager;

import com.pm.ordersystem.access.ClinicianAccess;
import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.access.StaffAccess;
import com.pm.ordersystem.command.*;
import com.pm.ordersystem.engine.TriagingEngine;
import com.pm.ordersystem.handler.OrderHandler;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderManagerTest {

    @Mock private OrderAccess orderAccess;
    @Mock private TriagingEngine triagingEngine;
    @Mock private OrderHandler orderHandler;
    @Mock private CommandLog commandLog;
    @Mock private NotificationService notificationService;
    @Mock private StaffAccess staffAccess;
    @Mock private ClinicianAccess clinicianAccess;

    private OrderManager orderManager;

    // ── helpers ───────────────────────────────────────────────────────
    private SubmitOrderCommand submitCmd(OrderType type,
                                         String patient,
                                         String clinician,
                                         String description,
                                         Priority priority) {
        return new SubmitOrderCommand(
                type, patient, clinician, description, priority,
                orderAccess, orderHandler, triagingEngine,
                List.of(notificationService)
        );
    }

    private ClaimOrderCommand claimCmd(String orderId,
                                       String claimedBy) {
        return new ClaimOrderCommand(
                orderId, claimedBy,
                orderAccess, staffAccess,
                List.of(notificationService)
        );
    }

    private CompleteOrderCommand completeCmd(String orderId,
                                             String actor) {
        return new CompleteOrderCommand(
                orderId, actor,
                orderAccess, staffAccess,
                List.of(notificationService)
        );
    }

    private CancelOrderCommand cancelCmd(String orderId,
                                         String actor) {
        return new CancelOrderCommand(
                orderId, actor,
                orderAccess, clinicianAccess,
                List.of(notificationService)
        );
    }

    @BeforeEach
    void setUp() {
        orderManager = new OrderManager(
                orderAccess,
                triagingEngine,
                orderHandler,
                commandLog,
                List.of(notificationService)
        );
    }

    // ── Submit Order ──────────────────────────────────────────────────

    @Test
    void submit_order_creates_and_saves_order() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());

        SubmitOrderCommand cmd = submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood glucose test",
                Priority.URGENT);

        // Act
        Order order = orderManager.handle(cmd);

        // Assert
        assertNotNull(order);
        assertEquals(OrderType.LAB, order.getType());
        assertEquals("John Smith", order.getPatientName());
        assertEquals("Dr. Jones", order.getClinician());
        assertEquals(Priority.URGENT, order.getPriority());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(orderAccess).saveOrder(order);
    }

    @Test
    void submit_order_notifies_clinician() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());

        SubmitOrderCommand cmd = submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood glucose test",
                Priority.URGENT);

        // Act
        orderManager.handle(cmd);

        // Assert
        verify(notificationService)
                .notifyClinician(eq("Dr. Jones"),
                        any(Order.class),
                        eq("SUBMITTED"));
    }

    @Test
    void submit_order_records_command() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());

        SubmitOrderCommand cmd = submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood glucose test",
                Priority.URGENT);

        // Act
        orderManager.handle(cmd);

        // Assert
        verify(commandLog).record(eq("SUBMIT"),
                anyString(),
                eq("Dr. Jones"),
                any(Command.class));
    }

    // ── Claim Order ───────────────────────────────────────────────────

    @Test
    void claim_order_sets_status_to_in_progress() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        when(staffAccess.exists("Nurse Williams"))
                .thenReturn(true);

        Order order = orderManager.handle(submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE));

        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act
        orderManager.handle(claimCmd(order.getId(),
                "Nurse Williams"));

        // Assert
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals("Nurse Williams", order.getClaimedBy());
    }

    @Test
    void claim_by_unregistered_staff_throws_exception() {
        // Arrange
        when(staffAccess.exists("Unknown Person"))
                .thenReturn(false);
        // ← remove the submit setup and findOrderById stub
        // validation fails before order is ever looked up

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(claimCmd("any-order-id",
                        "Unknown Person")));
    }

    @Test
    void claim_already_claimed_order_throws_exception() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        when(staffAccess.exists("Nurse Williams"))
                .thenReturn(true);

        Order order = orderManager.handle(submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE));

        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy("Nurse A");

        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act + Assert — second claim attempt fails
        assertThrows(IllegalStateException.class, () ->
                orderManager.handle(claimCmd(order.getId(),
                        "Nurse Williams")));
    }

    // ── Complete Order ────────────────────────────────────────────────

    @Test
    void complete_order_sets_status_to_completed() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        when(staffAccess.exists("Nurse Williams"))
                .thenReturn(true);

        Order order = orderManager.handle(submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE));

        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy("Nurse Williams");

        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act
        orderManager.handle(completeCmd(order.getId(),
                "Nurse Williams"));

        // Assert
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void complete_by_different_staff_throws_exception() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        when(staffAccess.exists("Nurse B"))
                .thenReturn(true);

        Order order = orderManager.handle(submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE));

        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setClaimedBy("Nurse A");

        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act + Assert — different staff cannot complete
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(completeCmd(order.getId(),
                        "Nurse B")));
    }

    @Test
    void complete_by_unregistered_staff_throws_exception() {
        // Arrange
        when(staffAccess.exists("Unknown"))
                .thenReturn(false);
        // ← remove the findOrderById stub
        // validation fails before order is ever looked up

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(completeCmd("any-order-id",
                        "Unknown")));
    }

    // ── Cancel Order ──────────────────────────────────────────────────

    @Test
    void cancel_order_sets_status_to_cancelled() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        when(clinicianAccess.exists("Dr. Jones"))
                .thenReturn(true);

        Order order = orderManager.handle(submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE));

        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act
        orderManager.handle(cancelCmd(order.getId(),
                "Dr. Jones"));

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancel_by_non_clinician_throws_exception() {
        // Arrange
        when(clinicianAccess.exists("Nurse Williams"))
                .thenReturn(false);

        // Act + Assert — staff cannot cancel
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(cancelCmd("order-1",
                        "Nurse Williams")));
    }

    @Test
    void cancel_by_different_clinician_throws_exception() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        when(clinicianAccess.exists("Dr. Smith"))
                .thenReturn(true);

        Order order = orderManager.handle(submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE));

        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act + Assert — different clinician cannot cancel
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(cancelCmd(order.getId(),
                        "Dr. Smith")));
    }

    @Test
    void cancel_non_pending_order_throws_exception() {
        // Arrange
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        when(clinicianAccess.exists("Dr. Jones"))
                .thenReturn(true);

        Order order = orderManager.handle(submitCmd(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE));

        order.setStatus(OrderStatus.IN_PROGRESS);

        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                orderManager.handle(cancelCmd(order.getId(),
                        "Dr. Jones")));
    }

    @Test
    void order_not_found_throws_exception() {
        // Arrange
        when(staffAccess.exists("Nurse Williams"))
                .thenReturn(true);
        when(orderAccess.findOrderById("nonexistent"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(claimCmd("nonexistent",
                        "Nurse Williams")));
    }
}