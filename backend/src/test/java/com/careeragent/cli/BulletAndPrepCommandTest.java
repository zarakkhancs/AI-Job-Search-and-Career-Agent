package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import com.careeragent.optimizer.BulletOptimizerService;
import com.careeragent.prep.PrepSheetService;
import com.careeragent.prompts.BulletPromptFactory;
import com.careeragent.prompts.PrepSheetPromptFactory;
import java.util.List;
import org.junit.jupiter.api.Test;

class BulletAndPrepCommandTest {
    private static BulletCommand bullet(String response) {
        return new BulletCommand(new BulletOptimizerService(new MockLLMClient(response), new BulletPromptFactory()));
    }

    private static PrepCommand prep(String response) {
        return new PrepCommand(new PrepSheetService(new MockLLMClient(response), new PrepSheetPromptFactory()));
    }

    @Test
    void bulletPrintsVersionsAndFlagsInventedFigures() {
        String out = bullet("{\"versions\":[\"Built a REST API cutting latency by 40%\"]}")
                .execute(List.of("Built", "a", "REST", "API"));
        assertTrue(out.contains("1. Built a REST API cutting latency by 40%"));
        assertTrue(out.contains("not in the original"));
        assertTrue(out.contains("40%"));
    }

    @Test
    void bulletWithoutArgsShowsUsage() {
        assertTrue(bullet("{}").execute(List.of()).startsWith("Usage"));
    }

    @Test
    void prepPrintsBothSections() {
        String out = prep("{\"technical\":[\"What is a hash map?\"],\"behavioral\":[\"Describe a deadline you met.\"]}")
                .execute(List.of("Backend", "developer", "role"));
        assertTrue(out.contains("Technical questions:"));
        assertTrue(out.contains("1. What is a hash map?"));
        assertTrue(out.contains("Behavioral questions:"));
    }

    @Test
    void prepReportsBadModelOutput() {
        assertTrue(prep("not json").execute(List.of("Backend", "role")).contains("failed"));
    }

    @Test
    void prepWithoutArgsShowsUsage() {
        assertTrue(prep("{}").execute(List.of()).startsWith("Usage"));
    }
}