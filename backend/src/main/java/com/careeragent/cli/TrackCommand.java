package com.careeragent.cli;

import com.careeragent.pipeline.ApplicationService;
import com.careeragent.pipeline.ApplicationView;
import com.careeragent.pipeline.InvalidTransitionException;
import java.util.List;

/** CLI for tracking applications: add, list and move them through the pipeline. */
public class TrackCommand implements Command {
    static final String USAGE = "Usage: track add <company> <title> | track add <company> | <title> | "
            + "track list [status] | track move <id> <apply|interview|offer|reject>";

    private final ApplicationService service;

    public TrackCommand(ApplicationService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "track";
    }

    @Override
    public String description() {
        return "Track job applications: track add | list | move";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return USAGE;
        }
        try {
            switch (args.get(0).toLowerCase()) {
                case "add":
                    return add(args.subList(1, args.size()));
                case "list":
                    return list(args.size() > 1 ? args.get(1) : null);
                case "move":
                    return move(args);
                default:
                    return USAGE;
            }
        } catch (IllegalArgumentException | InvalidTransitionException e) {
            return e.getMessage();
        }
    }

    private String add(List<String> rest) {
        if (rest.isEmpty()) {
            return USAGE;
        }
        String joined = String.join(" ", rest);
        String company;
        String title;
        if (joined.contains("|")) {
            String[] parts = joined.split("\\|", 2);
            company = parts[0].trim();
            title = parts[1].trim();
        } else {
            company = rest.get(0);
            title = String.join(" ", rest.subList(1, rest.size())).trim();
        }
        if (company.isEmpty() || title.isEmpty()) {
            return USAGE;
        }
        int id = service.add(company, title);
        return "Added #" + id + ": " + company + " - " + title + " (SAVED)";
    }

    private String list(String status) {
        List<ApplicationView> views = status == null ? service.list() : service.list(status);
        if (views.isEmpty()) {
            return "No applications.";
        }
        StringBuilder sb = new StringBuilder();
        for (ApplicationView v : views) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("#").append(v.id()).append(" ").append(v.company()).append(" - ")
                    .append(v.title()).append(" [").append(v.status()).append("]");
        }
        return sb.toString();
    }

    private String move(List<String> args) {
        if (args.size() < 3) {
            return USAGE;
        }
        int id;
        try {
            id = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            return "Application id must be a number.";
        }
        ApplicationView v = service.move(id, args.get(2));
        return "Application #" + v.id() + " is now " + v.status() + ".";
    }
}
