package com.careeragent.cli;

import java.util.List;

/** Command pattern: each CLI action is an object with a name and an execute method. */
public interface Command {
    String name();

    String description();

    String execute(List<String> args);
}
