package com.careeragent.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import org.junit.jupiter.api.Test;

class PlannerTest {
    private static final String TOOL_CALL = "{\"tool\":\"echo\",\"args\":{\"text\":\"hi\"}}";

    private ToolRegistry registry() {
        ToolRegistry r = new ToolRegistry();
        r.register(TestTools.echo());
        return r;
    }

    @Test
    void parsesToolCall() throws Exception {
        Planner p = new Planner(new MockLLMClient(TOOL_CALL), registry());
        PlanStep step = p.plan("say hi");
        assertFalse(step.isDirectAnswer());
        assertEquals("echo", step.toolName());
        assertEquals("hi", step.args().get("text"));
    }

    @Test
    void parsesToolCallInsideCodeFence() throws Exception {
        String fence = "\u0060\u0060\u0060";
        String raw = fence + "json\n" + TOOL_CALL + "\n" + fence;
        Planner p = new Planner(new MockLLMClient(raw), registry());
        assertEquals("echo", p.plan("say hi").toolName());
    }

    @Test
    void parsesDirectAnswer() throws Exception {
        Planner p = new Planner(new MockLLMClient("{\"tool\":\"none\",\"answer\":\"Hello\"}"), registry());
        PlanStep step = p.plan("greet me");
        assertTrue(step.isDirectAnswer());
        assertEquals("Hello", step.directAnswer());
    }

    @Test
    void rejectsUnknownTool() {
        Planner p = new Planner(new MockLLMClient("{\"tool\":\"hack\",\"args\":{}}"), registry());
        assertThrows(PlanningException.class, () -> p.plan("do it"));
    }

    @Test
    void rejectsInvalidJson() {
        Planner p = new Planner(new MockLLMClient("I think you should use echo"), registry());
        assertThrows(PlanningException.class, () -> p.plan("do it"));
    }

    @Test
    void promptListsToolsAndRequest() throws Exception {
        MockLLMClient mock = new MockLLMClient(TOOL_CALL);
        new Planner(mock, registry()).plan("do it");
        String prompt = mock.getPrompts().get(0);
        assertTrue(prompt.contains("echo"));
        assertTrue(prompt.contains("do it"));
    }

    @Test
    void blankRequestRejectedWithoutCallingLlm() {
        MockLLMClient mock = new MockLLMClient(TOOL_CALL);
        Planner p = new Planner(mock, registry());
        assertThrows(IllegalArgumentException.class, () -> p.plan("  "));
        assertTrue(mock.getPrompts().isEmpty());
    }
}
