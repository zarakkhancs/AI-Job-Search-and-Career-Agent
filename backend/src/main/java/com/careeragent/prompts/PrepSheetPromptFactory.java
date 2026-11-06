package com.careeragent.prompts;

import java.util.List;
import java.util.Map;

/** Concrete creator for the interview prep sheet (F07). */
public class PrepSheetPromptFactory extends PromptFactory {
    @Override
    protected List<String> requiredInputs() {
        return List.of("job");
    }

    @Override
    protected Prompt createPrompt(Map<String, String> inputs) {
        return new PrepSheetPrompt(inputs.get("job").trim(), optional(inputs, "resume"));
    }
}