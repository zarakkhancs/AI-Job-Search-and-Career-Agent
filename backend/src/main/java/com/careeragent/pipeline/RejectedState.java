package com.careeragent.pipeline;

/** Terminal state: no further transitions. */
public class RejectedState implements ApplicationState {
    @Override
    public String name() {
        return "REJECTED";
    }
}
