package com.careeragent.alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Subject in the Observer pattern: stores alerts and notifies subscribed observers. */
public class AlertService {
    private final List<AlertObserver> observers = new CopyOnWriteArrayList<>();
    private final List<Alert> alerts = new ArrayList<>();
    private int nextId = 1;

    public void subscribe(AlertObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer must not be null.");
        }
        observers.add(observer);
    }

    public void unsubscribe(AlertObserver observer) {
        observers.remove(observer);
    }

    public Alert publish(String message, AlertSource source) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Alert message must not be blank.");
        }
        if (source == null) {
            throw new IllegalArgumentException("Alert source must not be null.");
        }
        Alert alert = new Alert(nextId++, message.trim(), source);
        alerts.add(alert);
        for (AlertObserver observer : observers) {
            try {
                observer.onAlert(alert);
            } catch (RuntimeException e) {
                // One faulty observer must not stop the others from being notified.
            }
        }
        return alert;
    }

    public List<Alert> unread() {
        return alerts.stream().filter(a -> !a.isRead()).toList();
    }

    public List<Alert> unread(AlertSource source) {
        return alerts.stream().filter(a -> !a.isRead() && a.source() == source).toList();
    }

    public void markRead(int id) {
        alerts.stream()
                .filter(a -> a.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No alert with id " + id))
                .markRead();
    }

    public int markAllRead() {
        int count = 0;
        for (Alert a : alerts) {
            if (!a.isRead()) {
                a.markRead();
                count++;
            }
        }
        return count;
    }
}
