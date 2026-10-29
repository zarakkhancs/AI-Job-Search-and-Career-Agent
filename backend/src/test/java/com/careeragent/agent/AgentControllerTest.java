package com.careeragent.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import org.junit.jupiter.api.Test;

class AgentControllerTest {

    @Test
    void finalAnswerIsBuiltFromToolResult() {
        ToolRegistry r = new ToolRegistry();
        r.register(TestTools.echo());
        MockLLMClient mock = new MockLLMClient(
                "{\"tool\":\"echo\",\"args\":{\"text\":\"hello\"}}", "Final: hello");
        String out = new AgentController(mock, r).handle("echo hello");
        assertEquals("Final: hello", out);
        assertEquals(2, mock.getPrompts().size());
        assertTrue(mock.getPrompts().get(1).contains("hello"));
    }

    @Test
    void directAnswerSkipsToolsAndSecondLlmCall() {
        MockLLMClient mock = new MockLLMClient("{\"tool\":\"none\",\"answer\":\"Hi\"}");
        String out = new AgentController(mock, new ToolRegistry()).handle("hello");
        assertEquals("Hi", out);
        assertEquals(1, mock.getPrompts().size());
    }

    @Test
    void toolFailureIsReportedAndNoAnswerIsInvented() {
        ToolRegistry r = new ToolRegistry();
        r.register(TestTools.failing());
        MockLLMClient mock = new MockLLMClient("{\"tool\":\"fail\",\"args\":{}}", "made-up answer");
        String out = new AgentController(mock, r).handle("break");
        assertTrue(out.contains("failed"));
        assertEquals(1, mock.getPrompts().size());
    }

    @Test
    void malformedPlanProducesFriendlyMessage() {
        MockLLMClient mock = new MockLLMClient("not json");
        String out = new AgentController(mock, new ToolRegistry()).handle("anything");
        assertTrue(out.startsWith("I could not plan"));
    }
}
