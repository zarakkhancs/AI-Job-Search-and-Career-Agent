package com.careeragent.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MatchJobToolTest {
    private final MatchJobTool tool = new MatchJobTool(new JobMatcher(new KeywordOverlapStrategy()));

    @Test
    void reportsScoreMatchedAndMissing() {
        String out = tool.execute(Map.of("resume_skills", "Java, Python", "job_skills", "Java, SQL"));
        assertTrue(out.contains("Match score: 50/100"));
        assertTrue(out.contains("strategy: keyword"));
        assertTrue(out.contains("Matched: Java"));
        assertTrue(out.contains("Missing: SQL"));
    }

    @Test
    void missingArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("job_skills", "Java")));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("resume_skills", "Java")));
    }

    @Test
    void csvSplittingTrimsAndSkipsEmptyItems() {
        assertEquals(List.of("a", "b"), MatchJobTool.split(" a , ,b ,"));
    }
}
