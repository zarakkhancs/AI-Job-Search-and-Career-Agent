package com.careeragent.cli;

import com.careeragent.alerts.Alert;
import com.careeragent.alerts.AlertService;
import com.careeragent.alerts.AlertSource;
import java.util.List;

/** CLI access to alerts: unread list, scheduler deadlines only, and marking as read. */
public class AlertsCommand implements Command {
    static final String USAGE = "Usage: alerts | alerts deadlines | alerts read <id|all>";

    private final AlertService service;

    public AlertsCommand(AlertService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "alerts";
    }

    @Override
    public String description() {
        return "Show unread alerts: alerts [deadlines | read <id|all>]";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return format(service.unread());
        }
        switch (args.get(0).toLowerCase()) {
            case "deadlines":
                return format(service.unread(AlertSource.SCHEDULER));
            case "read":
                return read(args);
            default:
                return USAGE;
        }
    }

    private String read(List<String> args) {
        if (args.size() < 2) {
            return USAGE;
        }
        if (args.get(1).equalsIgnoreCase("all")) {
            return "Marked " + service.markAllRead() + " alert(s) as read.";
        }
        int id;
        try {
            id = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            return "Alert id must be a number.";
        }
        try {
            service.markRead(id);
            return "Marked alert #" + id + " as read.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private static String format(List<Alert> alerts) {
        if (alerts.isEmpty()) {
            return "No unread alerts.";
        }
        StringBuilder sb = new StringBuilder();
        for (Alert a : alerts) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("#").append(a.id()).append(" [").append(a.source()).append("] ").append(a.message());
        }
        return sb.toString();
    }
}
