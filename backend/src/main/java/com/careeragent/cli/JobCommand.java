package com.careeragent.cli;

import com.careeragent.ingestion.JobIngestException;
import com.careeragent.ingestion.JobIngestionService;
import com.careeragent.ingestion.JobPosting;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** CLI for F02: import job postings from a URL or a text file, list them and show them. */
public class JobCommand implements Command {
    static final String USAGE = String.join("\n",
            "Usage:",
            "  job add <url-or-text-file>",
            "  job list",
            "  job show <id>");
    private static final int PREVIEW = 200;

    private final JobIngestionService service;

    public JobCommand(JobIngestionService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "job";
    }

    @Override
    public String description() {
        return "Import job postings: job add <url-or-file> | job list | job show <id>";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return USAGE;
        }
        try {
            return switch (args.get(0).toLowerCase(Locale.ROOT)) {
                case "add" -> add(args);
                case "list" -> list();
                case "show" -> show(args);
                default -> USAGE;
            };
        } catch (JobIngestException | IllegalArgumentException e) {
            return "Could not import the job posting: " + e.getMessage();
        }
    }

    private String add(List<String> args) {
        if (args.size() < 2) {
            return USAGE;
        }
        String target = args.get(1);
        JobPosting posting;
        if (target.toLowerCase(Locale.ROOT).startsWith("http://") || target.toLowerCase(Locale.ROOT).startsWith("https://")) {
            posting = service.ingest(target);
        } else {
            try {
                posting = service.ingestText(target, Files.readString(Path.of(target)));
            } catch (IOException e) {
                return "Could not read file: " + e.getMessage();
            }
        }
        String preview = posting.text().length() > PREVIEW ? posting.text().substring(0, PREVIEW) + "..." : posting.text();
        return "Imported job posting #" + posting.id() + " (" + posting.source() + "): "
                + posting.text().length() + " characters.\n" + preview;
    }

    private String list() {
        List<JobPosting> all = service.list();
        if (all.isEmpty()) {
            return "No job postings imported yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (JobPosting p : all) {
            sb.append("#").append(p.id()).append(" ").append(p.source())
                    .append(" (").append(p.text().length()).append(" characters)\n");
        }
        return sb.toString().stripTrailing();
    }

    private String show(List<String> args) {
        if (args.size() < 2) {
            return USAGE;
        }
        try {
            return service.get(Integer.parseInt(args.get(1))).text();
        } catch (NumberFormatException e) {
            return "The id must be a whole number.";
        }
    }
}