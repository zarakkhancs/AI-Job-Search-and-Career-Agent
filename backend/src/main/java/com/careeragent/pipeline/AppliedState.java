package com.careeragent.pipeline;

public class AppliedState implements ApplicationState {
    @Override
    public String name() {
        return "APPLIED";
    }

    @Override
    public ApplicationState scheduleInterview() {
        return new InterviewState();
    }

    @Override
    public ApplicationState reject() {
        return new RejectedState();
    }
}
