package com.careeragent.alerts;

public class Alert {
    private final int id;
    private final String message;
    private final AlertSource source;
    private boolean read;

    Alert(int id, String message, AlertSource source) {
        this.id = id;
        this.message = message;
        this.source = source;
    }

    public int id() {
        return id;
    }

    public String message() {
        return message;
    }

    public AlertSource source() {
        return source;
    }

    public boolean isRead() {
        return read;
    }

    void markRead() {
        this.read = true;
    }
}
