package com.pm.ordersystem.command;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class CommandLog {

    private final List<CommandLogEntry> entries = new ArrayList<>();

    // record with command object for replay
    public void record(String commandType,
                       String orderId,
                       String actor,
                       Command command) {
        entries.add(new CommandLogEntry(
                commandType, orderId, actor, command));
    }

    public List<CommandLogEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    // get last entry for undo
    public Optional<CommandLogEntry> getLastEntry() {
        if (entries.isEmpty()) return Optional.empty();
        return Optional.of(entries.get(entries.size() - 1));
    }

    //  remove last entry after undo
    public void removeLastEntry() {
        if (!entries.isEmpty()) {
            entries.remove(entries.size() - 1);
        }
    }

    //  find by id for replay
    public Optional<CommandLogEntry> findById(String id) {
        return entries.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }
}