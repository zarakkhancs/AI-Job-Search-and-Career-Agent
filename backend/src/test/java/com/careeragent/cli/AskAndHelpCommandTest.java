package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.LLMException;
import com.careeragent.llm.MockLLMClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class AskAndHelpCommandTest {

    @Test
    void helpListsRegisteredCommands() {
        CommandRegistry r = new CommandRegistry();
        r.register(new HelpCommand(r));
        r.register(new AskCommand(new MockLLMClient()));
        String out = r.dispatch("help");
        assertTrue(out.contains("help"));
        assertTrue(out.contains("ask"));
    }

    @Test
    void askSendsJoinedQuestionToLlmAndReturnsAnswer() throws LLMException {
        MockLLMClient mock = new MockLLMClient("42");
        AskCommand ask = new AskCommand(mock);
        assertEquals("42", ask.execute(List.of("what", "is", "life")));
        assertEquals(List.of("what is life"), mock.getPrompts());
    }

    @Test
    void askWithoutQuestionShowsUsageAndSkipsLlm() {
        MockLLMClient mock = new MockLLMClient("unused");
        AskCommand ask = new AskCommand(mock);
        assertTrue(ask.execute(List.of()).startsWith("Usage"));
        assertTrue(mock.getPrompts().isEmpty());
    }

    @Test
    void askReportsLlmFailureInsteadOfCrashing() {
        AskCommand ask = new AskCommand(new MockLLMClient());
        assertTrue(ask.execute(List.of("hi")).startsWith("AI request failed"));
    }
}
