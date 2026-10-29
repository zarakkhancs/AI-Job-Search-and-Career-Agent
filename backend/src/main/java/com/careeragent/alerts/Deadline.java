package com.careeragent.alerts;

import java.time.LocalDate;

public record Deadline(String label, LocalDate date) {
    public Deadline {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Deadline label must not be blank.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Deadline date must not be null.");
        }
    }
}
