package com.careeragent.agent;

import java.util.Map;

/** The planner's decision: call a tool, or answer directly. */
public record PlanStep(String toolName, Map<String, String> args, String directAnswer) {

    public static PlanStep toolCall(String toolName, Map<String, String> args) {
        return new PlanStep(toolName, Map.copyOf(args), null);
    }

    public static PlanStep answer(String text) {
        return new PlanStep(null, Map.of(), text);
    }

    public boolean isDirectAnswer() {
        return directAnswer != null;
    }
}
