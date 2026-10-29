package com.careeragent.agent;

import java.util.Map;

final class TestTools {
    private TestTools() {
    }

    static Tool echo() {
        return new Tool() {
            public String name() { return "echo"; }
            public String description() { return "Echoes the text argument"; }
            public String execute(Map<String, String> args) { return args.getOrDefault("text", ""); }
        };
    }

    static Tool failing() {
        return new Tool() {
            public String name() { return "fail"; }
            public String description() { return "Always fails"; }
            public String execute(Map<String, String> args) { throw new IllegalStateException("boom"); }
        };
    }
}
