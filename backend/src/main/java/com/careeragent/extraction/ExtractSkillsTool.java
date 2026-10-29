package com.careeragent.extraction;

import com.careeragent.agent.Tool;
import com.careeragent.llm.LLMException;
import java.util.List;
import java.util.Map;

/** Agent tool wrapping SkillExtractor so the planner can call it. */
public class ExtractSkillsTool implements Tool {
    private final SkillExtractor extractor;

    public ExtractSkillsTool(SkillExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public String name() {
        return "extract_skills";
    }

    @Override
    public String description() {
        return "Extract verified skills from resume or job posting text. Args: text";
    }

    @Override
    public String execute(Map<String, String> args) {
        String text = args.get("text");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("extract_skills requires a 'text' argument.");
        }
        try {
            ExtractionResult r = extractor.extract(text);
            return "Verified skills: " + format(r.verified())
                    + ". Unverified (removed): " + format(r.rejected()) + ".";
        } catch (LLMException e) {
            throw new IllegalStateException("AI request failed: " + e.getMessage(), e);
        }
    }

    private static String format(List<String> items) {
        return items.isEmpty() ? "none" : String.join(", ", items);
    }
}
