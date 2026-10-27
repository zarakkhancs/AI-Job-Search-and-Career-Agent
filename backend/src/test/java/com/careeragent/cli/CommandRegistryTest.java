package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CommandRegistryTest {

    private static Command echo() {
        return new Command() {
            public String name() { return "echo"; }
            public String description() { return "Echo args"; }
            public String execute(List<String> args) { return String.join("|", args); }
        };
    }

    @Test
    void dispatchesToRegisteredCommandWithArgs() {
        CommandRegistry r = new CommandRegistry();
        r.register(echo());
        assertEquals("a|b", r.dispatch("echo a b"));
    }

    @Test
    void commandNamesAreCaseInsensitive() {
        CommandRegistry r = new CommandRegistry();
        r.register(echo());
        assertEquals("x", r.dispatch("ECHO x"));
    }

    @Test
    void unknownCommandGivesHelpfulMessage() {
        CommandRegistry r = new CommandRegistry();
        assertTrue(r.dispatch("nope").contains("Unknown command"));
    }

    @Test
    void blankInputReturnsEmptyString() {
        CommandRegistry r = new CommandRegistry();
        assertEquals("", r.dispatch("   "));
        assertEquals("", r.dispatch(null));
    }

    @Test
    void commandFailureIsReportedNotThrown() {
        CommandRegistry r = new CommandRegistry();
        r.register(new Command() {
            public String name() { return "boom"; }
            public String description() { return "Always fails"; }
            public String execute(List<String> args) { throw new IllegalStateException("bad"); }
        });
        assertEquals("Error: bad", r.dispatch("boom"));
    }
}
