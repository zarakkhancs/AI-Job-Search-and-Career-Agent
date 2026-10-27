package com.careeragent.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class MockLLMClientTest {

    @Test
    void returnsQueuedResponsesInOrder() throws Exception {
        MockLLMClient mock = new MockLLMClient("one", "two");
        assertEquals("one", mock.generate("a"));
        assertEquals("two", mock.generate("b"));
    }

    @Test
    void recordsPrompts() throws Exception {
        MockLLMClient mock = new MockLLMClient("x");
        mock.generate("my prompt");
        assertEquals(List.of("my prompt"), mock.getPrompts());
    }

    @Test
    void failsWhenNoResponseQueued() {
        MockLLMClient mock = new MockLLMClient();
        assertThrows(LLMException.class, () -> mock.generate("anything"));
    }
}
