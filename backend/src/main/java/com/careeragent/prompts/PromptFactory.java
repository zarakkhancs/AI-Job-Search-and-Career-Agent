package com.careeragent.prompts;

import java.util.List;
import java.util.Map;

/**
 * Factory Method pattern (creator). Every AI feature needs a differently structured prompt with
 * its own required inputs. Subclasses decide which concrete Prompt to build; this class validates
 * the inputs once, so services depend only on this abstraction and never assemble prompt strings.
 */
public abstract class PromptFactory {
    /** The factory method: each concrete factory returns its own kind of Prompt. */
    protected abstract Prompt createPrompt(Map<String, String> inputs);

    /** Names of the inputs that must be non-blank. */
    protected abstract List<String> requiredInputs();

    public final Prompt create(Map<String, String> inputs) {
        for (String key : requiredInputs()) {
            String value = inputs.get(key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Input '" + key + "' must not be blank.");
            }
        }
        return createPrompt(inputs);
    }

    protected static String optional(Map<String, String> inputs, String key) {
        String value = inputs.get(key);
        return value == null ? "" : value.trim();
    }
}