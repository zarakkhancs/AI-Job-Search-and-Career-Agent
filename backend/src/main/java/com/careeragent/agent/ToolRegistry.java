package com.careeragent.agent;

import java.util.Optional;
import java.util.Map;
import java.util.TreeMap;

/** Holds the tools available to the agent and describes them for the planner prompt. */
public class ToolRegistry {
    private final Map<String, Tool> tools = new TreeMap<>();

    public void register(Tool tool) {
        tools.put(tool.name(), tool);
    }

    public Optional<Tool> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public String describeAll() {
        StringBuilder sb = new StringBuilder();
        for (Tool t : tools.values()) {
            sb.append("- ").append(t.name()).append(": ").append(t.description()).append("\n");
        }
        return sb.toString();
    }
}
