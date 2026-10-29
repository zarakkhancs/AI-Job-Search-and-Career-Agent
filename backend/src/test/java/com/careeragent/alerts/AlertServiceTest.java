package com.careeragent.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertServiceTest {

    @Test
    void publishStoresAlertAndNotifiesObservers() {
        AlertService s = new AlertService();
        List<Alert> seen = new ArrayList<>();
        s.subscribe(seen::add);
        Alert a = s.publish("Hello", AlertSource.USER_EVENT);
        assertEquals(1, seen.size());
        assertEquals(a, seen.get(0));
        assertEquals(1, s.unread().size());
    }

    @Test
    void unreadFiltersBySource() {
        AlertService s = new AlertService();
        s.publish("user thing", AlertSource.USER_EVENT);
        s.publish("deadline", AlertSource.SCHEDULER);
        assertEquals(1, s.unread(AlertSource.SCHEDULER).size());
        assertEquals("deadline", s.unread(AlertSource.SCHEDULER).get(0).message());
        assertEquals(2, s.unread().size());
    }

    @Test
    void markReadRemovesFromUnread() {
        AlertService s = new AlertService();
        Alert a = s.publish("x", AlertSource.USER_EVENT);
        s.markRead(a.id());
        assertTrue(s.unread().isEmpty());
        assertTrue(a.isRead());
    }

    @Test
    void markReadUnknownIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> new AlertService().markRead(99));
    }

    @Test
    void blankMessageAndNullSourceRejected() {
        AlertService s = new AlertService();
        assertThrows(IllegalArgumentException.class, () -> s.publish("  ", AlertSource.USER_EVENT));
        assertThrows(IllegalArgumentException.class, () -> s.publish("ok", null));
    }

    @Test
    void failingObserverDoesNotBlockOthers() {
        AlertService s = new AlertService();
        List<Alert> seen = new ArrayList<>();
        s.subscribe(a -> {
            throw new IllegalStateException("bad observer");
        });
        s.subscribe(seen::add);
        s.publish("still delivered", AlertSource.USER_EVENT);
        assertEquals(1, seen.size());
    }

    @Test
    void unsubscribedObserverIsNotNotified() {
        AlertService s = new AlertService();
        List<Alert> seen = new ArrayList<>();
        AlertObserver o = seen::add;
        s.subscribe(o);
        s.unsubscribe(o);
        s.publish("nobody listening", AlertSource.USER_EVENT);
        assertTrue(seen.isEmpty());
    }

    @Test
    void markAllReadReturnsCount() {
        AlertService s = new AlertService();
        s.publish("a", AlertSource.USER_EVENT);
        s.publish("b", AlertSource.SCHEDULER);
        assertEquals(2, s.markAllRead());
        assertEquals(0, s.markAllRead());
    }

    @Test
    void consoleObserverPrintsSourceAndMessage() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        AlertService s = new AlertService();
        s.subscribe(new ConsoleAlertObserver(new PrintStream(buffer)));
        s.publish("Interview tomorrow", AlertSource.SCHEDULER);
        String out = buffer.toString();
        assertTrue(out.contains("SCHEDULER"));
        assertTrue(out.contains("Interview tomorrow"));
    }
}
