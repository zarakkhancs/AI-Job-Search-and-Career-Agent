package com.careeragent.pipeline;

public class SavedState implements ApplicationState {
    @Override
    public String name() {
        return "SAVED";
    }

    @Override
    public ApplicationState apply() {
        return new AppliedState();
    }
}
