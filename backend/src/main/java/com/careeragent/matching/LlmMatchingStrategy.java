package com.careeragent.matching;

import com.careeragent.llm.LLMClient;
import com.careeragent.llm.LLMException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The LLM judges the overall fit score; matched and missing skills are still computed in code. */
public class LlmMatchingStrategy implements MatchingStrategy {
    private static final Pattern NUMBER = Pattern.compile("\\d+");

    private final LLMClient llm;

    public LlmMatchingStrategy(LLMClient llm) {
        this.llm = llm;
    }

    @Override
    public String name() {
        return "llm";
    }

    @Override
    public MatchResult match(List<String> resumeSkills, List<String> jobSkills) {
        SkillComparison.Result r = SkillComparison.compare(resumeSkills, jobSkills);
        String raw;
        try {
            raw = llm.generate(buildPrompt(resumeSkills, jobSkills));
        } catch (LLMException e) {
            throw new IllegalStateException("AI request failed: " + e.getMessage(), e);
        }
        return new MatchResult(parseScore(raw), r.matched(), r.missing());
    }

    String buildPrompt(List<String> resumeSkills, List<String> jobSkills) {
        return String.join("\n",
                "Rate how well a candidate fits a job from 0 to 100, considering related skills too.",
                "Candidate skills: " + String.join(", ", resumeSkills == null ? List.of() : resumeSkills),
                "Job required skills: " + String.join(", ", jobSkills),
                "Respond with a single integer only.");
    }

    static int parseScore(String raw) {
        Matcher m = NUMBER.matcher(raw == null ? "" : raw);
        if (!m.find()) {
            throw new IllegalStateException("The AI did not return a numeric score.");
        }
        long value = Long.parseLong(m.group().length() > 9 ? "100" : m.group());
        return (int) Math.min(100, value);
    }
}
