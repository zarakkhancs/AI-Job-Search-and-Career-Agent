package com.careeragent.pipeline;

/** State pattern: each state decides which transitions are legal and returns the next state. */
public interface ApplicationState {
    String name();

    default ApplicationState apply() {
        throw invalid("apply");
    }

    default ApplicationState scheduleInterview() {
        throw invalid("schedule an interview");
    }

    default ApplicationState receiveOffer() {
        throw invalid("receive an offer");
    }

    default ApplicationState reject() {
        throw invalid("reject");
    }

    private InvalidTransitionException invalid(String action) {
        return new InvalidTransitionException("Cannot " + action + " from state " + name());
    }
}
