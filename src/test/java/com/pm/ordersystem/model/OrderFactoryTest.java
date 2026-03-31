package com.pm.ordersystem.model;

import com.pm.ordersystem.model.enums.OrderType;
import com.pm.ordersystem.model.enums.Priority;
import com.pm.ordersystem.model.order.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderFactoryTest {

    @Test
    void create_lab_order_returns_lab_order() {
        // Arrange
        OrderType type = OrderType.LAB;

        // Act
        Order order = OrderFactory.create(type, "John Smith",
                "Dr. Jones", "Blood test", Priority.ROUTINE);

        // Assert
        assertInstanceOf(LabOrder.class, order);
        assertEquals(OrderType.LAB, order.getType());
        assertEquals("John Smith", order.getPatientName());
        assertEquals("Dr. Jones", order.getClinician());
        assertEquals(Priority.ROUTINE, order.getPriority());
    }

    @Test
    void create_medication_order_returns_medication_order() {
        // Arrange
        OrderType type = OrderType.MEDICATION;

        // Act
        Order order = OrderFactory.create(type, "Jane Doe",
                "Dr. Smith", "Paracetamol 500mg", Priority.URGENT);

        // Assert
        assertInstanceOf(MedicationOrder.class, order);
        assertEquals(OrderType.MEDICATION, order.getType());
        assertEquals(Priority.URGENT, order.getPriority());
    }

    @Test
    void create_imaging_order_returns_imaging_order() {
        // Arrange
        OrderType type = OrderType.IMAGING;

        // Act
        Order order = OrderFactory.create(type, "Bob Lee",
                "Dr. Brown", "Chest X-Ray", Priority.STAT);

        // Assert
        assertInstanceOf(ImagingOrder.class, order);
        assertEquals(OrderType.IMAGING, order.getType());
        assertEquals(Priority.STAT, order.getPriority());
    }

    @Test
    void create_null_type_throws_exception() {
        // Arrange — nothing

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                OrderFactory.create(null, "John Smith",
                        "Dr. Jones", "Blood test", Priority.ROUTINE));
    }

    @Test
    void created_order_has_pending_status() {
        // Arrange + Act
        Order order = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Assert
        assertEquals(com.pm.ordersystem.model.enums.OrderStatus.PENDING,
                order.getStatus());
    }

    @Test
    void created_order_has_unique_id() {
        // Arrange + Act
        Order order1 = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);
        Order order2 = OrderFactory.create(OrderType.LAB,
                "John Smith", "Dr. Jones",
                "Blood test", Priority.ROUTINE);

        // Assert
        assertNotEquals(order1.getId(), order2.getId());
    }
}