package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.agent.AgentController;
import com.careeragent.agent.ToolRegistry;
import com.careeragent.extraction.ExtractSkillsTool;
import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.MockLLMClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgentCommandTest {

    @Test
    void emptyRequestShowsUsageAndSkipsLlm() {
        MockLLMClient mock = new MockLLMClient("unused");
        AgentCommand cmd = new AgentCommand(new AgentController(mock, new ToolRegistry()));
        assertTrue(cmd.execute(List.of()).startsWith("Usage"));
        assertTrue(mock.getPrompts().isEmpty());
    }

    @Test
    void directAnswerIsReturned() {
        MockLLMClient mock = new MockLLMClient("{\"tool\":\"none\",\"answer\":\"Hi there\"}");
        AgentCommand cmd = new AgentCommand(new AgentController(mock, new ToolRegistry()));
        assertEquals("Hi there", cmd.execute(List.of("say", "hi")));
    }

    @Test
    void fullToolPipelinePlanExtractAnswer() {
        MockLLMClient mock = new MockLLMClient(
                "{\"tool\":\"extract_skills\",\"args\":{\"text\":\"Java developer\"}}",
                "[\"Java\",\"Rust\"]",
                "You list Java.");
        ToolRegistry tools = new ToolRegistry();
        tools.register(new ExtractSkillsTool(new SkillExtractor(mock)));
        AgentCommand cmd = new AgentCommand(new AgentController(mock, tools));
        assertEquals("You list Java.", cmd.execute(List.of("extract", "skills", "from", "Java", "developer")));
        assertEquals(3, mock.getPrompts().size());
        assertTrue(mock.getPrompts().get(2).contains("Unverified (removed): Rust"));
    }

    @Test
    void unplannableRequestGivesFriendlyMessage() {
        MockLLMClient mock = new MockLLMClient("garbage");
        AgentCommand cmd = new AgentCommand(new AgentController(mock, new ToolRegistry()));
        assertTrue(cmd.execute(List.of("do", "something")).startsWith("I could not plan"));
    }
}
