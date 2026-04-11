package com.pm.ordersystem.handler;

import com.pm.ordersystem.access.OrderAccess;
import com.pm.ordersystem.command.CommandLog;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecoratorChainTest {

    @Mock private OrderAccess orderAccess;
    @Mock private CommandLog commandLog;

    private Clock fixedClock;
    private OrderHandler chain;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        when(orderAccess.listPendingOrders()).thenReturn(new ArrayList<>());

        OrderHandler base = new BaseOrderHandler();
        OrderHandler withAudit = new AuditLoggingDecorator(base);
        OrderHandler withBoost = new PriorityBoostDecorator(withAudit);
        OrderHandler withStatAudit = new StatAuditDecorator(withBoost, commandLog);
        OrderHandler withEscalation = new PriorityEscalationDecorator(
                withStatAudit, fixedClock, orderAccess);
        chain = new ValidationDecorator(withEscalation);
    }

    @Test
    void validation_rejects_blank_patient_name() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB, "",
                "Dr. Jones", "desc", Priority.URGENT);
        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> chain.handle(order));
    }

    @Test
    void validation_rejects_blank_clinician() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB, "John",
                "", "desc", Priority.URGENT);
        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> chain.handle(order));
    }

    @Test
    void valid_order_passes_through_chain() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB, "John",
                "Dr. Jones", "blood test", Priority.ROUTINE);
        // Act + Assert
        assertDoesNotThrow(() -> chain.handle(order));
    }

    @Test
    void priority_boost_upgrades_emergency_keyword_to_stat() {
        // Arrange
        Order order = OrderFactory.create(OrderType.LAB, "John",
                "Dr. Jones", "cardiac emergency", Priority.URGENT);
        // Act
        chain.handle(order);
        // Assert
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void escalation_upgrades_urgent_within_window_when_stat_arrives() {
        // Arrange
        Order urgentOrder = OrderFactory.create(OrderType.LAB, "P1",
                "Dr. A", "blood test", Priority.URGENT);

        when(orderAccess.listPendingOrders()).thenReturn(List.of(urgentOrder));

        Order statOrder = OrderFactory.create(OrderType.LAB, "P2",
                "Dr. A", "critical test", Priority.STAT);

        // Act — submit the STAT order; escalation should fire
        chain.handle(statOrder);

        // Assert — urgentOrder was escalated
        assertEquals(Priority.STAT, urgentOrder.getPriority());
    }

    @Test
    void escalation_does_not_upgrade_different_type() {
        // Arrange
        Order urgentMed = OrderFactory.create(OrderType.MEDICATION, "P1",
                "Dr. A", "medication", Priority.URGENT);

        when(orderAccess.listPendingOrders()).thenReturn(List.of(urgentMed));

        Order statLab = OrderFactory.create(OrderType.LAB, "P2",
                "Dr. A", "lab test", Priority.STAT);

        // Act
        chain.handle(statLab);

        // Assert — different type, should NOT be escalated
        assertEquals(Priority.URGENT, urgentMed.getPriority());
    }
}