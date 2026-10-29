package com.careeragent.alerts;

/** Observer pattern: anything that wants to be told when a new alert is published. */
public interface AlertObserver {
    void onAlert(Alert alert);
}
