package com.careeragent.alerts;

import java.io.PrintStream;

/** Concrete observer that prints each new alert as it arrives. */
public class ConsoleAlertObserver implements AlertObserver {
    private final PrintStream out;

    public ConsoleAlertObserver(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onAlert(Alert alert) {
        out.println("[ALERT/" + alert.source() + "] " + alert.message());
    }
}
