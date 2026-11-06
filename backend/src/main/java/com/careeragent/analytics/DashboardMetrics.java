package com.careeragent.analytics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Snapshot of the application pipeline. Rates are percentages of submitted applications. */
public record DashboardMetrics(
        int total,
        int submitted,
        Map<String, Integer> byStage,
        double interviewRate,
        double offerRate,
        double rejectionRate) {
    public DashboardMetrics {
        byStage = Collections.unmodifiableMap(new LinkedHashMap<>(byStage));
    }
}