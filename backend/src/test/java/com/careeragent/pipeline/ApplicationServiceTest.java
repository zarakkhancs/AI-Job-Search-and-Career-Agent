package com.careeragent.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.alerts.AlertService;
import com.careeragent.alerts.AlertSource;
import org.junit.jupiter.api.Test;

class ApplicationServiceTest {
    private final AlertService alerts = new AlertService();
    private final ApplicationService service = new ApplicationService(alerts);

    @Test
    void addAssignsSequentialIds() {
        assertEquals(1, service.add("Acme", "Dev"));
        assertEquals(2, service.add("Beta", "QA"));
    }

    @Test
    void newApplicationStartsSaved() {
        service.add("Acme", "Dev");
        assertEquals("SAVED", service.list().get(0).status());
    }

    @Test
    void moveFollowsStateRules() {
        int id = service.add("Acme", "Dev");
        assertEquals("APPLIED", service.move(id, "apply").status());
        assertEquals("INTERVIEW", service.move(id, "interview").status());
        assertEquals("OFFER", service.move(id, "offer").status());
    }

    @Test
    void illegalMoveThrowsAndKeepsStatus() {
        int id = service.add("Acme", "Dev");
        assertThrows(InvalidTransitionException.class, () -> service.move(id, "offer"));
        assertEquals("SAVED", service.list().get(0).status());
    }

    @Test
    void unknownIdAndUnknownActionRejected() {
        int id = service.add("Acme", "Dev");
        assertThrows(IllegalArgumentException.class, () -> service.move(99, "apply"));
        assertThrows(IllegalArgumentException.class, () -> service.move(id, "teleport"));
        assertThrows(IllegalArgumentException.class, () -> service.move(id, null));
    }

    @Test
    void successfulMovePublishesUserEventAlert() {
        int id = service.add("Acme", "Dev");
        service.move(id, "apply");
        assertEquals(1, alerts.unread().size());
        assertEquals(AlertSource.USER_EVENT, alerts.unread().get(0).source());
        assertTrue(alerts.unread().get(0).message().contains("APPLIED"));
    }

    @Test
    void failedMovePublishesNoAlert() {
        int id = service.add("Acme", "Dev");
        assertThrows(InvalidTransitionException.class, () -> service.move(id, "offer"));
        assertTrue(alerts.unread().isEmpty());
    }

    @Test
    void listFiltersByStatusIgnoringCase() {
        int a = service.add("Acme", "Dev");
        service.add("Beta", "QA");
        service.move(a, "apply");
        assertEquals(1, service.list("applied").size());
        assertEquals("Acme", service.list("APPLIED").get(0).company());
        assertEquals(1, service.list("saved").size());
    }

    @Test
    void invalidApplicationDataRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.add(" ", "Dev"));
        assertThrows(IllegalArgumentException.class, () -> service.add("Acme", ""));
    }

    @Test
    void nullAlertServiceRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ApplicationService(null));
    }
}
