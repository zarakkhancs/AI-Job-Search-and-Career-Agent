package com.careeragent.llm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Scripted LLMClient for tests: returns queued responses and records every prompt. */
public class MockLLMClient implements LLMClient {
    private final Deque<String> responses = new ArrayDeque<>();
    private final List<String> prompts = new ArrayList<>();

    public MockLLMClient(String... queuedResponses) {
        for (String r : queuedResponses) {
            responses.add(r);
        }
    }

    @Override
    public String generate(String prompt) throws LLMException {
        prompts.add(prompt);
        if (responses.isEmpty()) {
            throw new LLMException("No mock response queued.");
        }
        return responses.poll();
    }

    public List<String> getPrompts() {
        return List.copyOf(prompts);
    }
}
