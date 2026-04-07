package com.pm.ordersystem.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommandLogTest {

    // ── mock command for testing ──────────────────────────────────────
    private static class MockCommand implements Command {
        @Override
        public void execute() { }
    }

    private CommandLog commandLog;
    private Command mockCommand;

    @BeforeEach
    void setUp() {
        // Arrange
        commandLog  = new CommandLog();
        mockCommand = new MockCommand();
    }

    @Test
    void record_adds_entry_to_log() {
        // Act
        commandLog.record("SUBMIT", "order-1",
                "Dr. Jones", mockCommand);

        // Assert
        assertEquals(1, commandLog.getEntries().size());
    }

    @Test
    void record_stores_correct_fields() {
        // Act
        commandLog.record("SUBMIT", "order-1",
                "Dr. Jones", mockCommand);

        // Assert
        CommandLogEntry entry = commandLog.getEntries().get(0);
        assertEquals("SUBMIT",   entry.getCommandType());
        assertEquals("order-1",  entry.getOrderId());
        assertEquals("Dr. Jones", entry.getActor());
        assertNotNull(entry.getTimestamp());
        assertNotNull(entry.getId());
        assertNotNull(entry.getCommand());
    }

    @Test
    void record_multiple_entries() {
        // Act
        commandLog.record("SUBMIT",   "order-1",
                "Dr. Jones", mockCommand);
        commandLog.record("CLAIM",    "order-1",
                "Nurse A",   mockCommand);
        commandLog.record("COMPLETE", "order-1",
                "Nurse A",   mockCommand);

        // Assert
        assertEquals(3, commandLog.getEntries().size());
    }

    @Test
    void getLastEntry_returns_most_recent() {
        // Arrange
        commandLog.record("SUBMIT", "order-1",
                "Dr. Jones", mockCommand);
        commandLog.record("CLAIM",  "order-1",
                "Nurse A",  mockCommand);

        // Act
        Optional<CommandLogEntry> last = commandLog.getLastEntry();

        // Assert
        assertTrue(last.isPresent());
        assertEquals("CLAIM", last.get().getCommandType());
    }

    @Test
    void getLastEntry_returns_empty_when_log_is_empty() {
        // Act
        Optional<CommandLogEntry> last = commandLog.getLastEntry();

        // Assert
        assertTrue(last.isEmpty());
    }

    @Test
    void removeLastEntry_removes_most_recent() {
        // Arrange
        commandLog.record("SUBMIT", "order-1",
                "Dr. Jones", mockCommand);
        commandLog.record("CLAIM",  "order-1",
                "Nurse A",  mockCommand);
        commandLog.record("COMPLETE", "order-1",
                "Nurse A",  mockCommand);

        // Act
        commandLog.removeLastEntry();

        // Assert
        assertEquals(2, commandLog.getEntries().size());
        assertEquals("CLAIM",
                commandLog.getLastEntry().get().getCommandType());
    }

    @Test
    void findById_returns_correct_entry() {
        // Arrange
        commandLog.record("SUBMIT", "order-1",
                "Dr. Jones", mockCommand);
        commandLog.record("CANCEL", "order-1",
                "Dr. Jones", mockCommand);

        String id = commandLog.getEntries().get(0).getId();

        // Act
        Optional<CommandLogEntry> found = commandLog.findById(id);

        // Assert
        assertTrue(found.isPresent());
        assertEquals("SUBMIT", found.get().getCommandType());
        assertEquals(id, found.get().getId());
    }

    @Test
    void findById_returns_empty_for_unknown_id() {
        // Arrange
        commandLog.record("SUBMIT", "order-1",
                "Dr. Jones", mockCommand);

        // Act
        Optional<CommandLogEntry> found =
                commandLog.findById("nonexistent-id");

        // Assert
        assertTrue(found.isEmpty());
    }
}