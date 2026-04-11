package com.pm.ordersystem.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommandLogTest {

    private CommandLog commandLog;
    private Command dummyCommand;

    @BeforeEach
    void setUp() {
        commandLog = new CommandLog();
        dummyCommand = () -> {}; // lambda implements Command
    }

    @Test
    void record_adds_entry() {
        // Act
        commandLog.record("SUBMIT", "order-1", "Dr. Jones", dummyCommand);
        // Assert
        assertEquals(1, commandLog.getEntries().size());
    }

    @Test
    void entry_has_correct_fields() {
        // Act
        commandLog.record("CLAIM", "order-2", "Nurse A", dummyCommand);
        CommandLogEntry entry = commandLog.getEntries().get(0);
        // Assert
        assertEquals("CLAIM",    entry.getCommandType());
        assertEquals("order-2",  entry.getOrderId());
        assertEquals("Nurse A",  entry.getActor());
        assertNotNull(entry.getId());
        assertNotNull(entry.getTimestamp());
        assertNotNull(entry.getCommand());
    }

    @Test
    void get_last_entry_returns_most_recent() {
        // Arrange
        commandLog.record("SUBMIT", "order-1", "Dr. A", dummyCommand);
        commandLog.record("CLAIM",  "order-2", "Nurse B", dummyCommand);
        // Act
        Optional<CommandLogEntry> last = commandLog.getLastEntry();
        // Assert
        assertTrue(last.isPresent());
        assertEquals("CLAIM", last.get().getCommandType());
    }

    @Test
    void get_last_entry_returns_empty_when_log_empty() {
        // Act + Assert
        assertTrue(commandLog.getLastEntry().isEmpty());
    }

    @Test
    void remove_last_entry_removes_most_recent() {
        // Arrange
        commandLog.record("SUBMIT", "order-1", "Dr. A", dummyCommand);
        commandLog.record("CLAIM",  "order-2", "Nurse B", dummyCommand);
        // Act
        commandLog.removeLastEntry();
        // Assert
        assertEquals(1, commandLog.getEntries().size());
        assertEquals("SUBMIT", commandLog.getEntries().get(0).getCommandType());
    }

    @Test
    void find_by_id_returns_correct_entry() {
        // Arrange
        commandLog.record("CANCEL", "order-3", "Dr. B", dummyCommand);
        String id = commandLog.getEntries().get(0).getId();
        // Act
        Optional<CommandLogEntry> found = commandLog.findById(id);
        // Assert
        assertTrue(found.isPresent());
        assertEquals("CANCEL", found.get().getCommandType());
    }

    @Test
    void find_by_id_returns_empty_for_unknown_id() {
        // Act + Assert
        assertTrue(commandLog.findById("nonexistent").isEmpty());
    }

    @Test
    void entries_list_is_unmodifiable() {
        // Arrange
        commandLog.record("SUBMIT", "order-1", "Dr. A", dummyCommand);
        // Act + Assert
        assertThrows(UnsupportedOperationException.class,
                () -> commandLog.getEntries().clear());
    }
}