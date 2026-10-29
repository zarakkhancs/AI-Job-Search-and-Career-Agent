package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.alerts.AlertService;
import com.careeragent.alerts.AlertSource;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertsCommandTest {

    @Test
    void emptyInboxMessage() {
        assertEquals("No unread alerts.", new AlertsCommand(new AlertService()).execute(List.of()));
    }

    @Test
    void listsUnreadAlertsWithSource() {
        AlertService s = new AlertService();
        s.publish("Follow up with recruiter", AlertSource.USER_EVENT);
        String out = new AlertsCommand(s).execute(List.of());
        assertTrue(out.contains("#1"));
        assertTrue(out.contains("USER_EVENT"));
        assertTrue(out.contains("Follow up with recruiter"));
    }

    @Test
    void deadlinesListsOnlySchedulerAlerts() {
        AlertService s = new AlertService();
        s.publish("user note", AlertSource.USER_EVENT);
        s.publish("Deadline in 2 day(s): Acme", AlertSource.SCHEDULER);
        String out = new AlertsCommand(s).execute(List.of("deadlines"));
        assertTrue(out.contains("Acme"));
        assertFalse(out.contains("user note"));
    }

    @Test
    void readMarksAlertAndHidesIt() {
        AlertService s = new AlertService();
        s.publish("x", AlertSource.USER_EVENT);
        AlertsCommand cmd = new AlertsCommand(s);
        assertEquals("Marked alert #1 as read.", cmd.execute(List.of("read", "1")));
        assertEquals("No unread alerts.", cmd.execute(List.of()));
    }

    @Test
    void readUnknownOrNonNumericIdGivesError() {
        AlertsCommand cmd = new AlertsCommand(new AlertService());
        assertTrue(cmd.execute(List.of("read", "99")).contains("No alert with id 99"));
        assertEquals("Alert id must be a number.", cmd.execute(List.of("read", "abc")));
    }

    @Test
    void unknownSubcommandOrMissingIdShowsUsage() {
        AlertsCommand cmd = new AlertsCommand(new AlertService());
        assertTrue(cmd.execute(List.of("bogus")).startsWith("Usage"));
        assertTrue(cmd.execute(List.of("read")).startsWith("Usage"));
    }

    @Test
    void readAllMarksEverything() {
        AlertService s = new AlertService();
        s.publish("a", AlertSource.USER_EVENT);
        s.publish("b", AlertSource.SCHEDULER);
        assertEquals("Marked 2 alert(s) as read.", new AlertsCommand(s).execute(List.of("read", "all")));
    }
}
