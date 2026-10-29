package com.careeragent.matching;

import java.util.List;

/** Score = percentage of required job skills that the resume lists. Fully deterministic. */
public class KeywordOverlapStrategy implements MatchingStrategy {
    @Override
    public String name() {
        return "keyword";
    }

    @Override
    public MatchResult match(List<String> resumeSkills, List<String> jobSkills) {
        SkillComparison.Result r = SkillComparison.compare(resumeSkills, jobSkills);
        int total = r.matched().size() + r.missing().size();
        int score = (int) Math.round(100.0 * r.matched().size() / total);
        return new MatchResult(score, r.matched(), r.missing());
    }
}
