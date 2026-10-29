package com.careeragent.alerts;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Scans deadlines and publishes SCHEDULER alerts for ones coming up soon. */
public class DeadlineScheduler {
    private final AlertService service;
    private final int daysAhead;
    private final Set<String> alreadyAlerted = new HashSet<>();

    public DeadlineScheduler(AlertService service, int daysAhead) {
        if (daysAhead < 0) {
            throw new IllegalArgumentException("daysAhead must not be negative.");
        }
        this.service = service;
        this.daysAhead = daysAhead;
    }

    /** Returns how many new alerts were created. */
    public int check(List<Deadline> deadlines, LocalDate today) {
        int created = 0;
        for (Deadline d : deadlines) {
            long days = ChronoUnit.DAYS.between(today, d.date());
            if (days < 0 || days > daysAhead) {
                continue;
            }
            if (!alreadyAlerted.add(d.label() + "|" + d.date())) {
                continue;
            }
            String when = days == 0 ? "today" : "in " + days + " day(s)";
            service.publish("Deadline " + when + ": " + d.label() + " (" + d.date() + ")",
                    AlertSource.SCHEDULER);
            created++;
        }
        return created;
    }
}
