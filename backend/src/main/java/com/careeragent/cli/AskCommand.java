package com.careeragent.cli;

import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import java.util.List;

/** Sends a question to the LLM through the LLMClient abstraction. */
public class AskCommand implements Command {
    private final LLMClient llm;

    public AskCommand(LLMClient llm) {
        this.llm = llm;
    }

    @Override
    public String name() {
        return "ask";
    }

    @Override
    public String description() {
        return "Ask the AI a question: ask <question>";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return "Usage: ask <question>";
        }
        try {
            return llm.generate(String.join(" ", args));
        } catch (LLMException e) {
            return "AI request failed: " + e.getMessage();
        }
    }
}
