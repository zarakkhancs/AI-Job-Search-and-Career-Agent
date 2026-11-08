package com.careeragent.cli;

import com.careeragent.llm.LLMException;
import com.careeragent.resume.ParsedResume;
import com.careeragent.resume.ResumeException;
import com.careeragent.resume.ResumeImportService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** CLI for F01: import a resume from a PDF or text file and see the skills found in it. */
public class ResumeCommand implements Command {
    static final String USAGE = String.join("\n",
            "Usage:",
            "  resume import <file.pdf|file.txt>",
            "  resume list",
            "  resume show <id>");

    private final ResumeImportService service;

    public ResumeCommand(ResumeImportService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "resume";
    }

    @Override
    public String description() {
        return "Import a resume and read its skills: resume import <file> | resume list | resume show <id>";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return USAGE;
        }
        try {
            return switch (args.get(0).toLowerCase(Locale.ROOT)) {
                case "import" -> importFile(args);
                case "list" -> list();
                case "show" -> show(args);
                default -> USAGE;
            };
        } catch (LLMException e) {
            return "The AI request failed: " + e.getMessage();
        } catch (ResumeException | IllegalArgumentException e) {
            return "Could not import the resume: " + e.getMessage();
        }
    }

    private String importFile(List<String> args) throws LLMException {
        if (args.size() < 2) {
            return USAGE;
        }
        Path path = Path.of(args.get(1));
        ParsedResume parsed;
        try {
            String fileName = path.getFileName().toString();
            if (fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                parsed = service.importPdf(fileName, Files.readAllBytes(path));
            } else {
                parsed = service.importText(fileName, Files.readString(path));
            }
        } catch (IOException e) {
            return "Could not read file: " + e.getMessage();
        }
        return describe("Imported resume #" + parsed.id() + " (" + parsed.filename() + "): "
                + parsed.text().length() + " characters.", parsed);
    }

    private String list() {
        List<ParsedResume> all = service.list();
        if (all.isEmpty()) {
            return "No resumes imported yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (ParsedResume r : all) {
            sb.append("#").append(r.id()).append(" ").append(r.filename())
                    .append(" (").append(r.skills().size()).append(" skills)\n");
        }
        return sb.toString().stripTrailing();
    }

    private String show(List<String> args) {
        if (args.size() < 2) {
            return USAGE;
        }
        try {
            ParsedResume parsed = service.get(Integer.parseInt(args.get(1)));
            return describe("Resume #" + parsed.id() + " (" + parsed.filename() + ")", parsed);
        } catch (NumberFormatException e) {
            return "The id must be a whole number.";
        }
    }

    private static String describe(String heading, ParsedResume parsed) {
        StringBuilder sb = new StringBuilder(heading).append("\n");
        sb.append("Skills: ").append(parsed.skills().isEmpty() ? "none found" : String.join(", ", parsed.skills()));
        if (!parsed.rejectedSkills().isEmpty()) {
            sb.append("\nRejected (not found in the text): ").append(String.join(", ", parsed.rejectedSkills()));
        }
        return sb.toString();
    }
}