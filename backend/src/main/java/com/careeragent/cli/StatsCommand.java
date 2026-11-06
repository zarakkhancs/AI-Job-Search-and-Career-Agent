package com.careeragent.cli;

import com.careeragent.analytics.DashboardMetrics;
import com.careeragent.analytics.MetricsService;
import java.util.List;
import java.util.Map;

/** CLI view of the F09 analytics dashboard. */
public class StatsCommand implements Command {
    private final MetricsService metrics;

    public StatsCommand(MetricsService metrics) {
        this.metrics = metrics;
    }

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public String description() {
        return "Show pipeline analytics: applications per stage and conversion rates";
    }

    @Override
    public String execute(List<String> args) {
        DashboardMetrics m = metrics.compute();
        if (m.total() == 0) {
            return "No applications yet. Add one with: track add <company> | <title>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Applications: ").append(m.total()).append(" (").append(m.submitted())
                .append(" submitted)");
        for (Map.Entry<String, Integer> e : m.byStage().entrySet()) {
            sb.append("\n  ").append(e.getKey()).append(": ").append(e.getValue());
        }
        sb.append("\nInterview rate: ").append(m.interviewRate()).append("% | Offer rate: ")
                .append(m.offerRate()).append("% | Rejection rate: ").append(m.rejectionRate())
                .append("%");
        return sb.toString();
    }
}