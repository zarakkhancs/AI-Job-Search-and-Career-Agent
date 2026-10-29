package com.careeragent.matching;

/** Strategy pattern: interchangeable ways of scoring how well a resume fits a job. */
public interface MatchingStrategy {
    String name();

    MatchResult match(java.util.List<String> resumeSkills, java.util.List<String> jobSkills);
}
