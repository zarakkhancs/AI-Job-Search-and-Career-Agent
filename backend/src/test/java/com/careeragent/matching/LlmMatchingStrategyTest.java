package com.careeragent.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class LlmMatchingStrategyTest {

    @Test
    void scoreComesFromLlmButSkillListsAreComputed() {
        LlmMatchingStrategy s = new LlmMatchingStrategy(new MockLLMClient("Score: 85"));
        MatchResult r = s.match(List.of("Java"), List.of("Java", "SQL"));
        assertEquals(85, r.score());
        assertEquals(List.of("Java"), r.matched());
        assertEquals(List.of("SQL"), r.missing());
    }

    @Test
    void scoreAboveHundredIsClamped() {
        LlmMatchingStrategy s = new LlmMatchingStrategy(new MockLLMClient("250"));
        assertEquals(100, s.match(List.of("Java"), List.of("Java")).score());
    }

    @Test
    void nonNumericResponseIsRejected() {
        LlmMatchingStrategy s = new LlmMatchingStrategy(new MockLLMClient("pretty good"));
        assertThrows(IllegalStateException.class, () -> s.match(List.of("Java"), List.of("Java")));
    }

    @Test
    void llmFailureIsWrapped() {
        LlmMatchingStrategy s = new LlmMatchingStrategy(new MockLLMClient());
        assertThrows(IllegalStateException.class, () -> s.match(List.of("Java"), List.of("Java")));
    }

    @Test
    void promptContainsBothSkillLists() {
        MockLLMClient mock = new MockLLMClient("70");
        new LlmMatchingStrategy(mock).match(List.of("Python"), List.of("Java"));
        String prompt = mock.getPrompts().get(0);
        assertTrue(prompt.contains("Python"));
        assertTrue(prompt.contains("Java"));
    }
}
