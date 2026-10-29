package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.alerts.AlertService;
import com.careeragent.pipeline.ApplicationService;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackCommandTest {
    private final TrackCommand cmd = new TrackCommand(new ApplicationService(new AlertService()));

    @Test
    void addWithPipeSeparatorAllowsMultiWordCompany() {
        String out = cmd.execute(List.of("add", "Big", "Corp", "|", "Software", "Engineer"));
        assertEquals("Added #1: Big Corp - Software Engineer (SAVED)", out);
    }

    @Test
    void addWithoutPipeUsesFirstWordAsCompany() {
        String out = cmd.execute(List.of("add", "Acme", "Backend", "Developer"));
        assertEquals("Added #1: Acme - Backend Developer (SAVED)", out);
    }

    @Test
    void addMissingTitleShowsUsage() {
        assertTrue(cmd.execute(List.of("add", "Acme")).startsWith("Usage"));
        assertTrue(cmd.execute(List.of("add")).startsWith("Usage"));
    }

    @Test
    void emptyListMessage() {
        assertEquals("No applications.", cmd.execute(List.of("list")));
    }

    @Test
    void listShowsApplicationsWithStatus() {
        cmd.execute(List.of("add", "Acme", "Dev"));
        String out = cmd.execute(List.of("list"));
        assertTrue(out.contains("#1 Acme - Dev [SAVED]"));
    }

    @Test
    void moveUpdatesStatus() {
        cmd.execute(List.of("add", "Acme", "Dev"));
        assertEquals("Application #1 is now APPLIED.", cmd.execute(List.of("move", "1", "apply")));
    }

    @Test
    void illegalMoveReportsReason() {
        cmd.execute(List.of("add", "Acme", "Dev"));
        assertTrue(cmd.execute(List.of("move", "1", "offer")).contains("Cannot"));
    }

    @Test
    void badIdOrMissingArgsHandled() {
        assertEquals("Application id must be a number.", cmd.execute(List.of("move", "x", "apply")));
        assertTrue(cmd.execute(List.of("move", "1")).startsWith("Usage"));
        assertTrue(cmd.execute(List.of("move", "7", "apply")).contains("No application with id 7"));
    }

    @Test
    void listFiltersByStatus() {
        cmd.execute(List.of("add", "Acme", "Dev"));
        cmd.execute(List.of("add", "Beta", "QA"));
        cmd.execute(List.of("move", "1", "apply"));
        String out = cmd.execute(List.of("list", "applied"));
        assertTrue(out.contains("Acme"));
        assertFalse(out.contains("Beta"));
    }

    @Test
    void unknownSubcommandShowsUsage() {
        assertTrue(cmd.execute(List.of("bogus")).startsWith("Usage"));
        assertTrue(cmd.execute(List.of()).startsWith("Usage"));
    }
}
