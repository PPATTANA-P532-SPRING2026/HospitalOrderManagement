package com.pm.ordersystem.manager;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.command.*;
import com.pm.ordersystem.engine.PriorityFirstStrategy;
import com.pm.ordersystem.engine.TriagingEngine;
import com.pm.ordersystem.handler.*;
import com.pm.ordersystem.model.enums.OrderStatus;
import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.notification.NotificationService;
import com.pm.ordersystem.resource.OrderStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderManagerTest {

    @Mock
    private NotificationService notificationService;

    private OrderManager orderManager;
    private OrderAccess orderAccess;
    private CommandLog commandLog;

    @BeforeEach
    void setUp() {
        // Arrange — real objects except NotificationService
        OrderStore store       = new OrderStore();
        orderAccess            = new OrderAccess(store);
        commandLog             = new CommandLog();
        TriagingEngine engine  = new TriagingEngine(
                new PriorityFirstStrategy());

        // build decorator chain
        OrderHandler base  = new BaseOrderHandler();
        OrderHandler audit = new AuditLoggingDecorator(base);
        OrderHandler boost = new PriorityBoostDecorator(audit);
        OrderHandler chain = new ValidationDecorator(boost);

        orderManager = new OrderManager(
                orderAccess, engine, chain,
                notificationService, commandLog);
    }

    @Test
    void submit_order_returns_order_with_pending_status() {
        // Arrange
        SubmitOrderCommand cmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);

        // Act
        Order order = orderManager.handle(cmd);

        // Assert
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getId());
    }

    @Test
    void submit_order_saves_to_store() {
        // Arrange
        SubmitOrderCommand cmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);

        // Act
        Order order = orderManager.handle(cmd);

        // Assert
        assertEquals(1, orderAccess.listAllOrders().size());
        assertEquals(order.getId(),
                orderAccess.listAllOrders().get(0).getId());
    }

    @Test
    void submit_order_fires_notification() {
        // Arrange
        SubmitOrderCommand cmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);

        // Act
        Order order = orderManager.handle(cmd);

        // Assert
        verify(notificationService, times(1))
                .notify(order, "SUBMITTED");
    }

    @Test
    void submit_order_records_command() {
        // Arrange
        SubmitOrderCommand cmd = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);

        // Act
        orderManager.handle(cmd);

        // Assert
        assertEquals(1, commandLog.getEntries().size());
        assertEquals("SUBMIT",
                commandLog.getEntries().get(0).getCommandType());
    }

    @Test
    void claim_order_changes_status_to_in_progress() {
        // Arrange — submit first
        SubmitOrderCommand submit = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);
        Order order = orderManager.handle(submit);

        // Act
        ClaimOrderCommand claim = new ClaimOrderCommand(
                order.getId(), "Staff A");
        orderManager.handle(claim);

        // Assert
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals("Staff A", order.getClaimedBy());
    }

    @Test
    void claim_non_pending_order_throws_exception() {
        // Arrange
        SubmitOrderCommand submit = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);
        Order order = orderManager.handle(submit);

        ClaimOrderCommand claim = new ClaimOrderCommand(
                order.getId(), "Staff A");
        orderManager.handle(claim);

        // Act + Assert — claim again should fail
        ClaimOrderCommand claimAgain = new ClaimOrderCommand(
                order.getId(), "Staff B");
        assertThrows(IllegalStateException.class, () ->
                orderManager.handle(claimAgain));
    }

    @Test
    void complete_order_changes_status_to_completed() {
        // Arrange
        SubmitOrderCommand submit = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);
        Order order = orderManager.handle(submit);

        ClaimOrderCommand claim = new ClaimOrderCommand(
                order.getId(), "Staff A");
        orderManager.handle(claim);

        // Act
        CompleteOrderCommand complete = new CompleteOrderCommand(
                order.getId(), "Staff A");
        orderManager.handle(complete);

        // Assert
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void cancel_pending_order_changes_status_to_cancelled() {
        // Arrange
        SubmitOrderCommand submit = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);
        Order order = orderManager.handle(submit);

        // Act
        CancelOrderCommand cancel = new CancelOrderCommand(
                order.getId(), "Dr. Jones");
        orderManager.handle(cancel);

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancel_non_pending_order_throws_exception() {
        // Arrange
        SubmitOrderCommand submit = new SubmitOrderCommand(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);
        Order order = orderManager.handle(submit);

        ClaimOrderCommand claim = new ClaimOrderCommand(
                order.getId(), "Staff A");
        orderManager.handle(claim);

        // Act + Assert — cannot cancel in-progress order
        CancelOrderCommand cancel = new CancelOrderCommand(
                order.getId(), "Dr. Jones");
        assertThrows(IllegalStateException.class, () ->
                orderManager.handle(cancel));
    }

    @Test
    void unknown_order_id_throws_exception() {
        // Arrange
        ClaimOrderCommand claim = new ClaimOrderCommand(
                "non-existent-id", "Staff A");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderManager.handle(claim));
    }
}
