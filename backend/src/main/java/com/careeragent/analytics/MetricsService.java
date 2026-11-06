package com.careeragent.analytics;

import com.careeragent.pipeline.ApplicationService;
import com.careeragent.pipeline.ApplicationView;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * F09: deterministic analytics over the application pipeline (no AI involved).
 * "Submitted" means any application that has left the SAVED stage. Rates are computed from each
 * application's current stage, so a rejection counts as not having reached the interview stage.
 */
public class MetricsService {
    static final List<String> STAGES = List.of("SAVED", "APPLIED", "INTERVIEW", "OFFER", "REJECTED");

    private final ApplicationService applications;

    public MetricsService(ApplicationService applications) {
        if (applications == null) {
            throw new IllegalArgumentException("ApplicationService must not be null.");
        }
        this.applications = applications;
    }

    public DashboardMetrics compute() {
        Map<String, Integer> byStage = new LinkedHashMap<>();
        for (String stage : STAGES) {
            byStage.put(stage, 0);
        }
        List<ApplicationView> all = applications.list();
        for (ApplicationView v : all) {
            byStage.merge(v.status().toUpperCase(Locale.ROOT), 1, Integer::sum);
        }
        int total = all.size();
        int submitted = total - byStage.get("SAVED");
        int reachedInterview = byStage.get("INTERVIEW") + byStage.get("OFFER");
        return new DashboardMetrics(
                total,
                submitted,
                byStage,
                percent(reachedInterview, submitted),
                percent(byStage.get("OFFER"), submitted),
                percent(byStage.get("REJECTED"), submitted));
    }

    static double percent(int part, int whole) {
        if (whole == 0) {
            return 0.0;
        }
        return Math.round(part * 1000.0 / whole) / 10.0;
    }
}