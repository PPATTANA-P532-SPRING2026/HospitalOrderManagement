package com.pm.ordersystem.manager;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.command.CancelOrderCommand;
import com.pm.ordersystem.command.ClaimOrderCommand;
import com.pm.ordersystem.command.CommandLog;
import com.pm.ordersystem.command.CompleteOrderCommand;
import com.pm.ordersystem.command.SubmitOrderCommand;
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

    @Mock
    private OrderAccess orderAccess;

    @Mock
    private TriagingEngine triagingEngine;

    @Mock
    private OrderHandler orderHandler;

    @Mock
    private CommandLog commandLog;

    @Mock
    private NotificationService notificationService;

    private OrderManager orderManager;

    @BeforeEach
    void setUp() {
        // Arrange — inject mocks into OrderManager
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
        SubmitOrderCommand cmd = new SubmitOrderCommand(
                OrderType.LAB,
                "John Smith",
                "Dr. Jones",
                "Blood glucose test",
                Priority.URGENT
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());

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
    void submit_order_notifies_observers() {
        // Arrange
        SubmitOrderCommand cmd = new SubmitOrderCommand(
                OrderType.LAB,
                "John Smith",
                "Dr. Jones",
                "Blood glucose test",
                Priority.URGENT
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());

        // Act
        orderManager.handle(cmd);

        // Assert
        verify(notificationService)
                .onOrderStatusChanged(any(Order.class), eq("SUBMITTED"));
    }

    @Test
    void submit_order_records_command() {
        // Arrange
        SubmitOrderCommand cmd = new SubmitOrderCommand(
                OrderType.LAB,
                "John Smith",
                "Dr. Jones",
                "Blood glucose test",
                Priority.URGENT
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());

        // Act
        orderManager.handle(cmd);

        // Assert
        verify(commandLog).record(eq("SUBMIT"), anyString(),
                eq("Dr. Jones"));
    }

    // ── Claim Order ───────────────────────────────────────────────────

    @Test
    void claim_order_sets_status_to_in_progress() {
        // Arrange
        SubmitOrderCommand submitCmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        Order order = orderManager.handle(submitCmd);

        ClaimOrderCommand claimCmd = new ClaimOrderCommand(
                order.getId(), "Nurse Williams"
        );
        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act
        orderManager.handle(claimCmd);

        // Assert
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals("Nurse Williams", order.getClaimedBy());
    }

    @Test
    void claim_order_that_is_not_pending_throws_exception() {
        // Arrange
        SubmitOrderCommand submitCmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        Order order = orderManager.handle(submitCmd);
        order.setStatus(OrderStatus.IN_PROGRESS);

        ClaimOrderCommand claimCmd = new ClaimOrderCommand(
                order.getId(), "Nurse Williams"
        );
        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                orderManager.handle(claimCmd));
    }

    // ── Complete Order ────────────────────────────────────────────────

    @Test
    void complete_order_sets_status_to_completed() {
        // Arrange
        SubmitOrderCommand submitCmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        Order order = orderManager.handle(submitCmd);
        order.setStatus(OrderStatus.IN_PROGRESS);

        CompleteOrderCommand completeCmd = new CompleteOrderCommand(
                order.getId(), "Nurse Williams"
        );
        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act
        orderManager.handle(completeCmd);

        // Assert
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(notificationService)
                .onOrderStatusChanged(any(Order.class), eq("COMPLETED"));
    }

    @Test
    void complete_order_that_is_not_in_progress_throws_exception() {
        // Arrange
        SubmitOrderCommand submitCmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        Order order = orderManager.handle(submitCmd);
        // order is still PENDING not IN_PROGRESS

        CompleteOrderCommand completeCmd = new CompleteOrderCommand(
                order.getId(), "Nurse Williams"
        );
        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                orderManager.handle(completeCmd));
    }

    // ── Cancel Order ──────────────────────────────────────────────────

    @Test
    void cancel_order_sets_status_to_cancelled() {
        // Arrange
        SubmitOrderCommand submitCmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        Order order = orderManager.handle(submitCmd);

        CancelOrderCommand cancelCmd = new CancelOrderCommand(
                order.getId(), "Dr. Jones"
        );
        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act
        orderManager.handle(cancelCmd);

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(notificationService)
                .onOrderStatusChanged(any(Order.class), eq("CANCELLED"));
    }

    @Test
    void cancel_order_that_is_not_pending_throws_exception() {
        // Arrange
        SubmitOrderCommand submitCmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE
        );
        when(orderAccess.listPendingOrders()).thenReturn(List.of());
        when(triagingEngine.assignPosition(any(), any()))
                .thenReturn(List.of());
        Order order = orderManager.handle(submitCmd);
        order.setStatus(OrderStatus.IN_PROGRESS);

        CancelOrderCommand cancelCmd = new CancelOrderCommand(
                order.getId(), "Dr. Jones"
        );
        when(orderAccess.findOrderById(order.getId()))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                orderManager.handle(cancelCmd));
    }

    @Test
    void order_not_found_throws_exception() {
        // Arrange
        when(orderAccess.findOrderById("nonexistent"))
                .thenReturn(Optional.empty());

        ClaimOrderCommand cmd = new ClaimOrderCommand(
                "nonexistent", "Nurse Williams"
        );

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(cmd));
    }
}