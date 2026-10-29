package com.careeragent.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordOverlapStrategyTest {
    private final KeywordOverlapStrategy strategy = new KeywordOverlapStrategy();

    @Test
    void fullMatchScoresHundred() {
        MatchResult r = strategy.match(List.of("java", "sql"), List.of("Java", "SQL"));
        assertEquals(100, r.score());
        assertEquals(List.of("Java", "SQL"), r.matched());
        assertTrue(r.missing().isEmpty());
    }

    @Test
    void partialMatchListsMissingSkills() {
        MatchResult r = strategy.match(List.of("Java"), List.of("Java", "SQL"));
        assertEquals(50, r.score());
        assertEquals(List.of("SQL"), r.missing());
    }

    @Test
    void noOverlapScoresZero() {
        MatchResult r = strategy.match(List.of("Python"), List.of("Java", "SQL"));
        assertEquals(0, r.score());
        assertEquals(2, r.missing().size());
    }

    @Test
    void duplicateJobSkillsCountOnce() {
        MatchResult r = strategy.match(List.of("Java"), List.of("Java", "java", "SQL"));
        assertEquals(50, r.score());
    }

    @Test
    void emptyJobSkillsRejected() {
        assertThrows(IllegalArgumentException.class, () -> strategy.match(List.of("Java"), List.of()));
    }

    @Test
    void nullResumeTreatedAsEmpty() {
        assertEquals(0, strategy.match(null, List.of("Java")).score());
    }
}
