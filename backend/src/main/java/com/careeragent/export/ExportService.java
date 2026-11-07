package com.careeragent.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic exporter (F10): compiles the tailored summary, cover letter and resume into one
 * plain-text file. If the factual check flagged skills, a review warning goes at the top.
 */
public class ExportService {
    private static final String RULE = "-".repeat(60);
    private static final int MAX_SLUG_LENGTH = 40;

    public ExportFile build(String label, String resume, String summary, String coverLetter,
                            List<String> flaggedSkills) {
        require(resume, "Resume");
        require(summary, "Summary");
        require(coverLetter, "Cover letter");
        List<String> flagged = flaggedSkills == null ? List.of() : flaggedSkills;

        StringBuilder sb = new StringBuilder("APPLICATION PACKAGE\n");
        if (label != null && !label.isBlank()) {
            sb.append(label.trim()).append("\n");
        }
        sb.append("\n");
        if (!flagged.isEmpty()) {
            sb.append("REVIEW BEFORE SENDING\n");
            sb.append("The generated text mentions these skills, but they were not found in your resume: ")
                    .append(String.join(", ", flagged)).append(".\n");
            sb.append("Check or remove them before you send this package.\n\n");
        }
        section(sb, "SUMMARY", summary);
        section(sb, "COVER LETTER", coverLetter);
        section(sb, "RESUME", resume);
        return new ExportFile(filename(label), sb.toString().stripTrailing() + "\n");
    }

    /** Writes the file into the directory (created if needed) and returns its path. */
    public Path save(ExportFile file, Path directory) throws IOException {
        Files.createDirectories(directory);
        Path target = directory.resolve(file.filename());
        Files.writeString(target, file.content(), StandardCharsets.UTF_8);
        return target;
    }

    static String filename(String label) {
        String slug = slug(label);
        return slug.isEmpty() ? "application-package.txt" : "application-package-" + slug + ".txt";
    }

    static String slug(String label) {
        if (label == null) {
            return "";
        }
        String s = label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("^-+|-+$", "");
        if (s.length() > MAX_SLUG_LENGTH) {
            s = s.substring(0, MAX_SLUG_LENGTH).replaceAll("-+$", "");
        }
        return s;
    }

    private static void section(StringBuilder sb, String title, String body) {
        sb.append(RULE).append("\n").append(title).append("\n").append(RULE).append("\n");
        sb.append(body.trim()).append("\n\n");
    }

    private static void require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
    }
}