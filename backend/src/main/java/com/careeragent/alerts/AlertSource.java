package com.careeragent.alerts;

/** Where an alert came from. Scheduler alerts are kept distinct from user-triggered ones. */
public enum AlertSource {
    SCHEDULER,
    USER_EVENT
}
