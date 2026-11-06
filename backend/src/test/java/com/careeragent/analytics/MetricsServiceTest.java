package com.careeragent.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.careeragent.alerts.AlertService;
import com.careeragent.pipeline.ApplicationService;
import org.junit.jupiter.api.Test;

class MetricsServiceTest {
    private final ApplicationService apps = new ApplicationService(new AlertService());
    private final MetricsService metrics = new MetricsService(apps);

    @Test
    void emptyPipelineHasZeroRates() {
        DashboardMetrics m = metrics.compute();
        assertEquals(0, m.total());
        assertEquals(0, m.submitted());
        assertEquals(0.0, m.interviewRate());
        assertEquals(0.0, m.offerRate());
        assertEquals(0, m.byStage().get("SAVED").intValue());
    }

    @Test
    void countsStagesAndComputesRates() {
        apps.add("A", "Dev"); // stays SAVED
        int b = apps.add("B", "Dev");
        apps.move(b, "apply"); // APPLIED
        int c = apps.add("C", "Dev");
        apps.move(c, "apply");
        apps.move(c, "interview"); // INTERVIEW
        int d = apps.add("D", "Dev");
        apps.move(d, "apply");
        apps.move(d, "interview");
        apps.move(d, "offer"); // OFFER
        int e = apps.add("E", "Dev");
        apps.move(e, "apply");
        apps.move(e, "reject"); // REJECTED

        DashboardMetrics m = metrics.compute();
        assertEquals(5, m.total());
        assertEquals(4, m.submitted());
        assertEquals(1, m.byStage().get("SAVED").intValue());
        assertEquals(1, m.byStage().get("APPLIED").intValue());
        assertEquals(1, m.byStage().get("INTERVIEW").intValue());
        assertEquals(1, m.byStage().get("OFFER").intValue());
        assertEquals(1, m.byStage().get("REJECTED").intValue());
        assertEquals(50.0, m.interviewRate());
        assertEquals(25.0, m.offerRate());
        assertEquals(25.0, m.rejectionRate());
    }

    @Test
    void roundsToOneDecimal() {
        assertEquals(33.3, MetricsService.percent(1, 3));
        assertEquals(66.7, MetricsService.percent(2, 3));
    }

    @Test
    void rejectsNullService() {
        assertThrows(IllegalArgumentException.class, () -> new MetricsService(null));
    }
}