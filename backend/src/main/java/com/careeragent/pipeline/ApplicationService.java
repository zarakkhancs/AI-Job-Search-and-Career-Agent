package com.careeragent.pipeline;

import com.careeragent.alerts.AlertService;
import com.careeragent.alerts.AlertSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Stores job applications and moves them through the State-pattern pipeline. */
public class ApplicationService {
    private final Map<Integer, JobApplication> applications = new LinkedHashMap<>();
    private final AlertService alerts;
    private int nextId = 1;

    public ApplicationService(AlertService alerts) {
        if (alerts == null) {
            throw new IllegalArgumentException("AlertService must not be null.");
        }
        this.alerts = alerts;
    }

    public int add(String company, String title) {
        JobApplication app = new JobApplication(company, title);
        int id = nextId++;
        applications.put(id, app);
        return id;
    }

    /** Actions: apply, interview, offer, reject. Illegal moves throw InvalidTransitionException. */
    public ApplicationView move(int id, String action) {
        JobApplication app = find(id);
        if (action == null) {
            throw new IllegalArgumentException("Action must not be null.");
        }
        switch (action.toLowerCase(Locale.ROOT)) {
            case "apply" -> app.apply();
            case "interview" -> app.scheduleInterview();
            case "offer" -> app.receiveOffer();
            case "reject" -> app.reject();
            default -> throw new IllegalArgumentException(
                    "Unknown action '" + action + "'. Use apply, interview, offer or reject.");
        }
        ApplicationView view = view(id, app);
        alerts.publish("Application #" + id + " (" + app.getCompany() + " - " + app.getTitle()
                + ") is now " + view.status(), AlertSource.USER_EVENT);
        return view;
    }

    public List<ApplicationView> list() {
        return applications.entrySet().stream().map(e -> view(e.getKey(), e.getValue())).toList();
    }

    public List<ApplicationView> list(String status) {
        return list().stream().filter(v -> v.status().equalsIgnoreCase(status)).toList();
    }

    private JobApplication find(int id) {
        JobApplication app = applications.get(id);
        if (app == null) {
            throw new IllegalArgumentException("No application with id " + id);
        }
        return app;
    }

    private static ApplicationView view(int id, JobApplication app) {
        return new ApplicationView(id, app.getCompany(), app.getTitle(), app.getStatus());
    }
}
