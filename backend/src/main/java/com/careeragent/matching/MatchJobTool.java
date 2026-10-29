package com.careeragent.matching;

import com.careeragent.agent.Tool;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Agent tool that scores a resume against a job using the current matching strategy. */
public class MatchJobTool implements Tool {
    private final JobMatcher matcher;

    public MatchJobTool(JobMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public String name() {
        return "match_job";
    }

    @Override
    public String description() {
        return "Score how well resume skills fit a job. Args: resume_skills, job_skills "
                + "(both comma-separated lists)";
    }

    @Override
    public String execute(Map<String, String> args) {
        String resume = args.get("resume_skills");
        String job = args.get("job_skills");
        if (resume == null || resume.isBlank()) {
            throw new IllegalArgumentException("match_job requires a 'resume_skills' argument.");
        }
        if (job == null || job.isBlank()) {
            throw new IllegalArgumentException("match_job requires a 'job_skills' argument.");
        }
        MatchResult r = matcher.match(split(resume), split(job));
        return "Match score: " + r.score() + "/100 (strategy: " + matcher.strategyName() + "). "
                + "Matched: " + format(r.matched()) + ". Missing: " + format(r.missing()) + ".";
    }

    static List<String> split(String csv) {
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static String format(List<String> items) {
        return items.isEmpty() ? "none" : String.join(", ", items);
    }
}
