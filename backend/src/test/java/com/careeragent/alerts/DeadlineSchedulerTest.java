package com.careeragent.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeadlineSchedulerTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 27);

    @Test
    void upcomingDeadlineCreatesSchedulerAlert() {
        AlertService s = new AlertService();
        DeadlineScheduler sch = new DeadlineScheduler(s, 3);
        int created = sch.check(List.of(new Deadline("Acme application", TODAY.plusDays(2))), TODAY);
        assertEquals(1, created);
        Alert a = s.unread().get(0);
        assertEquals(AlertSource.SCHEDULER, a.source());
        assertTrue(a.message().contains("in 2 day(s)"));
        assertTrue(a.message().contains("Acme application"));
    }

    @Test
    void deadlineOutsideWindowIsIgnored() {
        AlertService s = new AlertService();
        int created = new DeadlineScheduler(s, 3).check(
                List.of(new Deadline("Far away", TODAY.plusDays(10))), TODAY);
        assertEquals(0, created);
        assertTrue(s.unread().isEmpty());
    }

    @Test
    void sameDeadlineIsNotAlertedTwice() {
        AlertService s = new AlertService();
        DeadlineScheduler sch = new DeadlineScheduler(s, 3);
        List<Deadline> list = List.of(new Deadline("Acme", TODAY.plusDays(1)));
        assertEquals(1, sch.check(list, TODAY));
        assertEquals(0, sch.check(list, TODAY));
        assertEquals(1, s.unread().size());
    }

    @Test
    void deadlineTodayAlertsWithTodayWording() {
        AlertService s = new AlertService();
        new DeadlineScheduler(s, 3).check(List.of(new Deadline("Submit", TODAY)), TODAY);
        assertTrue(s.unread().get(0).message().contains("today"));
    }

    @Test
    void pastDeadlineIsIgnored() {
        AlertService s = new AlertService();
        int created = new DeadlineScheduler(s, 3).check(
                List.of(new Deadline("Old", TODAY.minusDays(1))), TODAY);
        assertEquals(0, created);
    }

    @Test
    void negativeWindowAndInvalidDeadlineRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DeadlineScheduler(new AlertService(), -1));
        assertThrows(IllegalArgumentException.class, () -> new Deadline(" ", TODAY));
        assertThrows(IllegalArgumentException.class, () -> new Deadline("x", null));
    }
}
