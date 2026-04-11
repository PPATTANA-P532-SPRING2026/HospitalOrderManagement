package com.pm.ordersystem.command;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class CommandLog {

    private final List<CommandLogEntry> entries = new ArrayList<>();

    public void record(String commandType, String orderId,
                       String actor, Command command) {
        entries.add(new CommandLogEntry(commandType, orderId,
                actor, command));
    }

    public List<CommandLogEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public Optional<CommandLogEntry> getLastEntry() {
        if (entries.isEmpty()) return Optional.empty();
        return Optional.of(entries.get(entries.size() - 1));
    }

    public void removeLastEntry() {
        if (!entries.isEmpty()) {
            entries.remove(entries.size() - 1);
        }
    }

    public Optional<CommandLogEntry> findById(String id) {
        return entries.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }
}