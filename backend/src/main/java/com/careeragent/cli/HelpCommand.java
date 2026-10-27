package com.careeragent.cli;

import java.util.List;

public class HelpCommand implements Command {
    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "List available commands";
    }

    @Override
    public String execute(List<String> args) {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (Command c : registry.getCommands()) {
            sb.append("\n  ").append(c.name()).append(" - ").append(c.description());
        }
        return sb.toString();
    }
}
