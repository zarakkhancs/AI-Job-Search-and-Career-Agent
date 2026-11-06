package com.careeragent.prompts;

import java.util.List;
import java.util.Map;

/** Concrete creator for the bullet optimizer (F08). */
public class BulletPromptFactory extends PromptFactory {
    @Override
    protected List<String> requiredInputs() {
        return List.of("bullet");
    }

    @Override
    protected Prompt createPrompt(Map<String, String> inputs) {
        return new BulletOptimizerPrompt(inputs.get("bullet").trim(), optional(inputs, "job"));
    }
}