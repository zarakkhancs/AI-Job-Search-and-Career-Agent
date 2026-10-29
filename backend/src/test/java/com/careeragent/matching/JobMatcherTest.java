package com.careeragent.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.careeragent.llm.MockLLMClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobMatcherTest {

    @Test
    void delegatesToCurrentStrategy() {
        JobMatcher m = new JobMatcher(new KeywordOverlapStrategy());
        assertEquals("keyword", m.strategyName());
        assertEquals(50, m.match(List.of("Java"), List.of("Java", "SQL")).score());
    }

    @Test
    void strategyCanBeSwappedAtRuntime() {
        JobMatcher m = new JobMatcher(new KeywordOverlapStrategy());
        m.setStrategy(new LlmMatchingStrategy(new MockLLMClient("90")));
        assertEquals("llm", m.strategyName());
        assertEquals(90, m.match(List.of("Java"), List.of("Java", "SQL")).score());
    }

    @Test
    void nullStrategyRejected() {
        assertThrows(IllegalArgumentException.class, () -> new JobMatcher(null));
    }
}
