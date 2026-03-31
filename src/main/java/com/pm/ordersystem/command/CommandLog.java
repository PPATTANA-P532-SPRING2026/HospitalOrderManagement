package com.pm.ordersystem.command;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class CommandLog {

    private final List<CommandLogEntry> entries = new ArrayList<>();

    public void record(String commandType,
                       String orderId,
                       String actor) {
        entries.add(new CommandLogEntry(commandType, orderId, actor));
    }

    public List<CommandLogEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}