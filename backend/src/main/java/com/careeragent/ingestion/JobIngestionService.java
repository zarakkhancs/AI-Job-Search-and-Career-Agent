package com.careeragent.ingestion;

import java.util.ArrayList;
import java.util.List;

/**
 * F02: imports a job posting from pasted text or a URL, cleans it into plain text and keeps it
 * in memory for the session. Deterministic: no AI is involved.
 */
public class JobIngestionService {
    static final int MIN_CHARS = 30;

    private final PageFetcher fetcher;
    private final List<JobPosting> postings = new ArrayList<>();

    public JobIngestionService(PageFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public JobPosting ingest(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Enter a job posting URL or paste the job text.");
        }
        String trimmed = input.trim();
        String source;
        String text;
        if (isUrl(trimmed)) {
            source = trimmed;
            text = HtmlText.toText(fetcher.fetch(trimmed));
        } else {
            source = "pasted text";
            text = HtmlText.normalize(trimmed);
        }
        return save(source, text);
    }

    /** Imports text that was read from a file (for example by the CLI). */
    public JobPosting ingestText(String source, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("The job posting text is empty.");
        }
        return save(source == null || source.isBlank() ? "pasted text" : source, HtmlText.normalize(text));
    }

    public List<JobPosting> list() {
        synchronized (postings) {
            return List.copyOf(postings);
        }
    }

    public JobPosting get(int id) {
        synchronized (postings) {
            return postings.stream().filter(p -> p.id() == id).findFirst()
                    .orElseThrow(() -> new JobIngestException("No job posting with id " + id + "."));
        }
    }

    private JobPosting save(String source, String text) {
        if (text.length() < MIN_CHARS) {
            throw new JobIngestException("Not enough readable text was found. Paste the job description instead.");
        }
        synchronized (postings) {
            JobPosting posting = new JobPosting(postings.size() + 1, source, text);
            postings.add(posting);
            return posting;
        }
    }

    static boolean isUrl(String value) {
        String lower = value.toLowerCase();
        return (lower.startsWith("http://") || lower.startsWith("https://")) && value.chars().noneMatch(Character::isWhitespace);
    }
}