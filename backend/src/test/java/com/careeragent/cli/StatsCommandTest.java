package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.alerts.AlertService;
import com.careeragent.analytics.MetricsService;
import com.careeragent.pipeline.ApplicationService;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatsCommandTest {
    private final ApplicationService apps = new ApplicationService(new AlertService());
    private final StatsCommand command = new StatsCommand(new MetricsService(apps));

    @Test
    void explainsHowToStartWhenEmpty() {
        assertTrue(command.execute(List.of()).contains("track add"));
    }

    @Test
    void showsCountsAndRates() {
        int id = apps.add("Acme", "Dev");
        apps.move(id, "apply");
        apps.move(id, "interview");
        String out = command.execute(List.of());
        assertTrue(out.contains("Applications: 1 (1 submitted)"));
        assertTrue(out.contains("INTERVIEW: 1"));
        assertTrue(out.contains("Interview rate: 100.0%"));
    }
}