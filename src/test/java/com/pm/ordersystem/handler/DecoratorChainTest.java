package com.pm.ordersystem.handler;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.Order;
import com.pm.ordersystem.model.order.OrderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecoratorChainTest {

    @Mock
    private OrderAccess orderAccess;

    private Clock fixedClock;
    private OrderHandler chain;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
                Instant.now(), ZoneId.systemDefault());

        // Week 1 + 2b chain without escalation
        OrderHandler base      = new BaseOrderHandler();
        OrderHandler auditLog  = new AuditLoggingDecorator(base);
        OrderHandler boost     = new PriorityBoostDecorator(auditLog);
        OrderHandler statAudit = new StatAuditDecorator(boost);
        chain = new ValidationDecorator(statAudit);
    }

    // ── Validation tests ──────────────────────────────────────────────

    @Test
    void valid_order_passes_through_chain() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test",
                Priority.ROUTINE);

        // Act + Assert
        assertDoesNotThrow(() -> chain.handle(order));
    }

    @Test
    void empty_patient_name_throws_exception() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.LAB, "",
                "Dr. Jones", "Blood test",
                Priority.ROUTINE);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                chain.handle(order));
    }

    @Test
    void empty_clinician_throws_exception() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "", "Blood test",
                Priority.ROUTINE);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                chain.handle(order));
    }

    @Test
    void empty_description_throws_exception() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "",
                Priority.ROUTINE);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                chain.handle(order));
    }

    // ── PriorityBoost tests ───────────────────────────────────────────

    @Test
    void emergency_keyword_boosts_routine_to_stat() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "emergency blood test",
                Priority.ROUTINE);

        // Act
        chain.handle(order);

        // Assert
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void emergency_keyword_boosts_urgent_to_stat() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "emergency blood test",
                Priority.URGENT);

        // Act
        chain.handle(order);

        // Assert
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void cardiac_keyword_boosts_urgent_to_stat() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.IMAGING, "John Smith",
                "Dr. Jones", "cardiac scan needed",
                Priority.URGENT);

        // Act
        chain.handle(order);

        // Assert
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void stat_order_not_boosted_further() {
        // Arrange
        Order order = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "emergency blood test",
                Priority.STAT);

        // Act
        chain.handle(order);

        // Assert
        assertEquals(Priority.STAT, order.getPriority());
    }

    // ── PriorityEscalation tests ──────────────────────────────────────

//    @Test
//    void urgent_escalated_when_recent_stat_exists() {
//        // Arrange
//        Order existingStat = OrderFactory.create(
//                OrderType.LAB, "Jane Doe",
//                "Dr. Smith", "Existing STAT",
//                Priority.STAT);
//
//        when(orderAccess.listAllOrders())
//                .thenReturn(List.of(existingStat));
//
//        Order urgentOrder = OrderFactory.create(
//                OrderType.LAB, "John Smith",
//                "Dr. Jones", "Blood test",
//                Priority.URGENT);
//
//        // Act
//        chain.handle(urgentOrder);
//
//        // Assert
//        assertEquals(Priority.STAT, urgentOrder.getPriority());
//    }

//    @Test
//    void urgent_not_escalated_for_different_type() {
//        // Arrange
//        Order existingStat = OrderFactory.create(
//                OrderType.MEDICATION, "Jane Doe",
//                "Dr. Smith", "Existing STAT",
//                Priority.STAT);
//
//        when(orderAccess.listAllOrders())
//                .thenReturn(List.of(existingStat));
//
//        Order urgentOrder = OrderFactory.create(
//                OrderType.LAB, "John Smith",
//                "Dr. Jones", "Blood test",
//                Priority.URGENT);
//
//        // Act
//        chain.handle(urgentOrder);
//
//        // Assert — different type not escalated
//        assertEquals(Priority.URGENT, urgentOrder.getPriority());
//    }

//    @Test
//    void stat_submission_escalates_existing_urgent_orders() {
//        // Arrange
//        Order existingUrgent = OrderFactory.create(
//                OrderType.LAB, "Jane Doe",
//                "Dr. Smith", "Existing URGENT",
//                Priority.URGENT);
//
//        when(orderAccess.listPendingOrders())
//                .thenReturn(List.of(existingUrgent));
//
//        Order statOrder = OrderFactory.create(
//                OrderType.LAB, "John Smith",
//                "Dr. Jones", "STAT Blood test",
//                Priority.STAT);
//
//        // Act
//        chain.handle(statOrder);
//
//        // Assert
//        assertEquals(Priority.STAT,
//                existingUrgent.getPriority());
//        verify(orderAccess).saveOrder(existingUrgent);
//    }

    // ── StatAudit tests ───────────────────────────────────────────────

    @Test
    void stat_audit_does_not_throw_for_stat_order() {
        // Arrange
        Order statOrder = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test",
                Priority.STAT);

        // Act + Assert
        assertDoesNotThrow(() -> chain.handle(statOrder));
    }

    @Test
    void stat_audit_does_not_throw_for_routine_order() {
        // Arrange
        Order routineOrder = OrderFactory.create(
                OrderType.LAB, "John Smith",
                "Dr. Jones", "Blood test",
                Priority.ROUTINE);

        // Act + Assert
        assertDoesNotThrow(() -> chain.handle(routineOrder));
    }
}