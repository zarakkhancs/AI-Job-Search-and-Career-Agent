package com.careeragent.agent;

import java.util.Map;

/** A capability the agent can invoke. Concrete tools do the deterministic work. */
public interface Tool {
    String name();

    String description();

    String execute(Map<String, String> args);
}
