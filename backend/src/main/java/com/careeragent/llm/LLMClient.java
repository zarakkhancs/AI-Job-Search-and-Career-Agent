package com.careeragent.llm;

/** Abstraction over any LLM provider. The rest of the app depends only on this interface. */
public interface LLMClient {
    String generate(String prompt) throws LLMException;
}
