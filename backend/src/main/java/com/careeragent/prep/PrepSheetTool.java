package com.careeragent.prep;

import com.careeragent.agent.Tool;
import com.careeragent.llm.LLMException;
import java.util.Map;

/** Agent tool wrapping PrepSheetService. */
public class PrepSheetTool implements Tool {
    private final PrepSheetService service;

    public PrepSheetTool(PrepSheetService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "prep_sheet";
    }

    @Override
    public String description() {
        return "Generate technical and behavioral interview questions for a job. Args: job, resume (optional)";
    }

    @Override
    public String execute(Map<String, String> args) {
        String job = args.get("job");
        if (job == null || job.isBlank()) {
            throw new IllegalArgumentException("prep_sheet requires a 'job' argument.");
        }
        try {
            return service.generate(job, args.get("resume")).describe();
        } catch (LLMException e) {
            throw new IllegalStateException("AI request failed: " + e.getMessage(), e);
        }
    }
}