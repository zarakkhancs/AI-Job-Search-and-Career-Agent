package com.careeragent.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JobApplicationTest {

    private JobApplication newApp() {
        return new JobApplication("Acme", "Software Engineer Co-op");
    }

    @Test
    void startsInSavedState() {
        assertEquals("SAVED", newApp().getStatus());
    }

    @Test
    void happyPathToOffer() {
        JobApplication app = newApp();
        app.apply();
        assertEquals("APPLIED", app.getStatus());
        app.scheduleInterview();
        assertEquals("INTERVIEW", app.getStatus());
        app.receiveOffer();
        assertEquals("OFFER", app.getStatus());
    }

    @Test
    void canBeRejectedAfterApplying() {
        JobApplication app = newApp();
        app.apply();
        app.reject();
        assertEquals("REJECTED", app.getStatus());
    }

    @Test
    void canBeRejectedAfterInterview() {
        JobApplication app = newApp();
        app.apply();
        app.scheduleInterview();
        app.reject();
        assertEquals("REJECTED", app.getStatus());
    }

    @Test
    void cannotSkipApplyingBeforeInterview() {
        assertThrows(InvalidTransitionException.class, () -> newApp().scheduleInterview());
    }

    @Test
    void cannotReceiveOfferWithoutInterview() {
        JobApplication app = newApp();
        app.apply();
        assertThrows(InvalidTransitionException.class, app::receiveOffer);
    }

    @Test
    void cannotApplyTwice() {
        JobApplication app = newApp();
        app.apply();
        assertThrows(InvalidTransitionException.class, app::apply);
    }

    @Test
    void rejectedIsTerminal() {
        JobApplication app = newApp();
        app.apply();
        app.reject();
        assertThrows(InvalidTransitionException.class, app::apply);
        assertThrows(InvalidTransitionException.class, app::scheduleInterview);
        assertEquals("REJECTED", app.getStatus());
    }

    @Test
    void offerIsTerminal() {
        JobApplication app = newApp();
        app.apply();
        app.scheduleInterview();
        app.receiveOffer();
        assertThrows(InvalidTransitionException.class, app::reject);
        assertEquals("OFFER", app.getStatus());
    }

    @Test
    void rejectsBlankCompanyOrTitle() {
        assertThrows(IllegalArgumentException.class, () -> new JobApplication(" ", "Dev"));
        assertThrows(IllegalArgumentException.class, () -> new JobApplication("Acme", null));
    }
}
