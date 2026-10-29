package com.careeragent.cli;

import com.careeragent.agent.AgentController;
import java.util.List;

/** CLI entry point to the agent: plans, calls tools, and answers from real tool results. */
public class AgentCommand implements Command {
    private final AgentController controller;

    public AgentCommand(AgentController controller) {
        this.controller = controller;
    }

    @Override
    public String name() {
        return "agent";
    }

    @Override
    public String description() {
        return "Let the AI agent plan and use tools: agent <request>";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return "Usage: agent <request>";
        }
        return controller.handle(String.join(" ", args));
    }
}
