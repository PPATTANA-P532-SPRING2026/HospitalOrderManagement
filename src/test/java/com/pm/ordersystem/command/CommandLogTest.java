package com.pm.ordersystem.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandLogTest {

    private CommandLog commandLog;

    @BeforeEach
    void setUp() {
        // Arrange
        commandLog = new CommandLog();
    }

    @Test
    void record_adds_entry_to_log() {
        // Arrange
        String orderId = "order-123";

        // Act
        commandLog.record("SUBMIT", orderId, "Dr. Jones");

        // Assert
        assertEquals(1, commandLog.getEntries().size());
    }

    @Test
    void record_stores_correct_command_type() {
        // Arrange + Act
        commandLog.record("CLAIM", "order-123", "Staff A");

        // Assert
        assertEquals("CLAIM",
                commandLog.getEntries().get(0).getCommandType());
    }

    @Test
    void record_stores_correct_actor() {
        // Arrange + Act
        commandLog.record("COMPLETE", "order-123", "Staff B");

        // Assert
        assertEquals("Staff B",
                commandLog.getEntries().get(0).getActor());
    }

    @Test
    void record_stores_correct_order_id() {
        // Arrange + Act
        commandLog.record("CANCEL", "order-456", "Dr. Smith");

        // Assert
        assertEquals("order-456",
                commandLog.getEntries().get(0).getOrderId());
    }

    @Test
    void multiple_records_stored_in_order() {
        // Arrange + Act
        commandLog.record("SUBMIT",   "order-1", "Dr. Jones");
        commandLog.record("CLAIM",    "order-1", "Staff A");
        commandLog.record("COMPLETE", "order-1", "Staff A");

        // Assert
        List<CommandLogEntry> entries = commandLog.getEntries();
        assertEquals(3, entries.size());
        assertEquals("SUBMIT",   entries.get(0).getCommandType());
        assertEquals("CLAIM",    entries.get(1).getCommandType());
        assertEquals("COMPLETE", entries.get(2).getCommandType());
    }

    @Test
    void entries_list_is_unmodifiable() {
        // Arrange
        commandLog.record("SUBMIT", "order-1", "Dr. Jones");

        // Act + Assert
        assertThrows(UnsupportedOperationException.class, () ->
                commandLog.getEntries().clear());
    }
}