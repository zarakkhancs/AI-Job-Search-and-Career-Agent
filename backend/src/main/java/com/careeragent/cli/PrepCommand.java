package com.careeragent.cli;

import com.careeragent.llm.LLMException;
import com.careeragent.prep.PrepException;
import com.careeragent.prep.PrepSheetService;
import java.util.List;

/** CLI for F07: prep <job posting text> [| <resume text>]. */
public class PrepCommand implements Command {
    static final String USAGE = "Usage: prep <job posting text> [| <resume text>]";

    private final PrepSheetService service;

    public PrepCommand(PrepSheetService service) {
        this.service = service;
    }

    @Override
    public String name() {
        return "prep";
    }

    @Override
    public String description() {
        return "Interview prep sheet: prep <job posting> [| <resume>]";
    }

    @Override
    public String execute(List<String> args) {
        if (args.isEmpty()) {
            return USAGE;
        }
        String joined = String.join(" ", args);
        String job = joined;
        String resume = null;
        if (joined.contains("|")) {
            String[] parts = joined.split("\\|", 2);
            job = parts[0].trim();
            resume = parts[1].trim();
        }
        if (job.isEmpty()) {
            return USAGE;
        }
        try {
            return service.generate(job, resume).describe();
        } catch (LLMException | PrepException e) {
            return "Prep sheet failed: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}