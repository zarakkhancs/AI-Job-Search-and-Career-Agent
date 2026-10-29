package com.careeragent.pipeline;

/** Read-only snapshot of a tracked application, for the CLI and GUI to display. */
public record ApplicationView(int id, String company, String title, String status) {
}
