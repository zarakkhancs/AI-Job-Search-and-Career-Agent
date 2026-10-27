package com.careeragent.pipeline;

/** A job application whose behaviour depends on its current pipeline state. */
public class JobApplication {
    private final String company;
    private final String title;
    private ApplicationState state = new SavedState();

    public JobApplication(String company, String title) {
        if (company == null || company.isBlank()) {
            throw new IllegalArgumentException("Company must not be blank.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank.");
        }
        this.company = company.trim();
        this.title = title.trim();
    }

    public String getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return state.name();
    }

    public void apply() {
        state = state.apply();
    }

    public void scheduleInterview() {
        state = state.scheduleInterview();
    }

    public void receiveOffer() {
        state = state.receiveOffer();
    }

    public void reject() {
        state = state.reject();
    }
}
