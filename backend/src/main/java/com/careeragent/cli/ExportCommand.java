package com.careeragent.cli;

import com.careeragent.documents.TailoredDocument;
import com.careeragent.documents.TailoredDocumentService;
import com.careeragent.export.ExportFile;
import com.careeragent.export.ExportService;
import com.careeragent.llm.LLMException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI for F10: tailors a resume and cover letter to a job (AI plus factual check), then saves
 * the whole application package as a text file.
 */
public class ExportCommand implements Command {
    static final String USAGE = "Usage: export <resume-file> <job-file> [label]";

    private final TailoredDocumentService documents;
    private final ExportService exports;
    private final Path outputDir;

    public ExportCommand(TailoredDocumentService documents, ExportService exports, Path outputDir) {
        this.documents = documents;
        this.exports = exports;
        this.outputDir = outputDir;
    }

    @Override
    public String name() {
        return "export";
    }

    @Override
    public String description() {
        return "Tailor a resume to a job and save the package: export <resume-file> <job-file> [label]";
    }

    @Override
    public String execute(List<String> args) {
        if (args.size() < 2) {
            return USAGE;
        }
        String resume;
        String job;
        try {
            resume = Files.readString(Path.of(args.get(0)));
            job = Files.readString(Path.of(args.get(1)));
        } catch (IOException e) {
            return "Could not read file: " + e.getMessage();
        }
        String label = args.size() > 2 ? String.join(" ", args.subList(2, args.size())) : null;

        TailoredDocument doc;
        try {
            doc = documents.tailor(resume, job);
        } catch (LLMException e) {
            return "The AI request failed: " + e.getMessage();
        } catch (RuntimeException e) {
            return "Could not tailor the documents: " + e.getMessage();
        }

        Path saved;
        try {
            ExportFile file = exports.build(label, resume, doc.summary(), doc.coverLetter(), doc.flaggedSkills());
            saved = exports.save(file, outputDir);
        } catch (IllegalArgumentException e) {
            return "Could not build the package: " + e.getMessage();
        } catch (IOException e) {
            return "Could not save the package: " + e.getMessage();
        }
        StringBuilder sb = new StringBuilder("Saved application package to " + saved);
        if (!doc.flaggedSkills().isEmpty()) {
            sb.append("\nWarning: not found in your resume: ").append(String.join(", ", doc.flaggedSkills()));
        }
        return sb.toString();
    }
}