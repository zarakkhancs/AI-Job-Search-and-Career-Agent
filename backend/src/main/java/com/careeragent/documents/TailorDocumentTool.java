package com.careeragent.documents;

import com.careeragent.agent.Tool;
import com.careeragent.llm.LLMException;
import java.util.List;
import java.util.Map;

/** Agent tool wrapping TailoredDocumentService so the planner can call it. */
public class TailorDocumentTool implements Tool {
    private final TailoredDocumentService service;

    public TailorDocumentTool(TailoredDocumentService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "tailor_documents";
    }

    @Override
    public String description() {
        return "Write a tailored resume summary and cover letter, with a factual check against the resume. Args: resume, job";
    }

    @Override
    public String execute(Map<String, String> args) {
        String resume = args.get("resume");
        String job = args.get("job");
        if (resume == null || resume.isBlank() || job == null || job.isBlank()) {
            throw new IllegalArgumentException("tailor_documents requires 'resume' and 'job' arguments.");
        }
        try {
            TailoredDocument d = service.tailor(resume, job);
            return "Summary: " + d.summary() + "\n\nCover letter: " + d.coverLetter()
                    + "\n\nSupported by resume: " + format(d.supportedSkills())
                    + ". Flagged (not in resume, verify before using): " + format(d.flaggedSkills()) + ".";
        } catch (LLMException e) {
            throw new IllegalStateException("AI request failed: " + e.getMessage(), e);
        }
    }

    private static String format(List<String> items) {
        return items.isEmpty() ? "none" : String.join(", ", items);
    }
}