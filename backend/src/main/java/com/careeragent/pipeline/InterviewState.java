package com.careeragent.pipeline;

public class InterviewState implements ApplicationState {
    @Override
    public String name() {
        return "INTERVIEW";
    }

    @Override
    public ApplicationState receiveOffer() {
        return new OfferState();
    }

    @Override
    public ApplicationState reject() {
        return new RejectedState();
    }
}
