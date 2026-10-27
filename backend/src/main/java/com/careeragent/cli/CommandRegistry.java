package com.careeragent.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Invoker: looks up the right Command for an input line and runs it. */
public class CommandRegistry {
    private final Map<String, Command> commands = new TreeMap<>();

    public void register(Command command) {
        commands.put(command.name().toLowerCase(), command);
    }

    public List<Command> getCommands() {
        return List.copyOf(commands.values());
    }

    public String dispatch(String line) {
        if (line == null || line.isBlank()) {
            return "";
        }
        String[] parts = line.trim().split("\\s+");
        Command command = commands.get(parts[0].toLowerCase());
        if (command == null) {
            return "Unknown command: " + parts[0] + ". Type 'help' to see available commands.";
        }
        try {
            return command.execute(Arrays.asList(parts).subList(1, parts.length));
        } catch (RuntimeException e) {
            return "Error: " + e.getMessage();
        }
    }
}
