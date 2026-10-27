package com.careeragent.pipeline;

/** Terminal state: no further transitions. */
public class OfferState implements ApplicationState {
    @Override
    public String name() {
        return "OFFER";
    }
}
