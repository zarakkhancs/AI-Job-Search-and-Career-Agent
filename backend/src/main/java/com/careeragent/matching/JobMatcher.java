package com.careeragent.matching;

import java.util.List;

/** Context for the Strategy pattern: delegates matching to the currently selected strategy. */
public class JobMatcher {
    private MatchingStrategy strategy;

    public JobMatcher(MatchingStrategy strategy) {
        setStrategy(strategy);
    }

    public final void setStrategy(MatchingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy must not be null.");
        }
        this.strategy = strategy;
    }

    public String strategyName() {
        return strategy.name();
    }

    public MatchResult match(List<String> resumeSkills, List<String> jobSkills) {
        return strategy.match(resumeSkills, jobSkills);
    }
}
