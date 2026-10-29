package com.careeragent.extraction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExtractSkillsToolTest {

    @Test
    void reportsVerifiedAndRemovedSkills() {
        ExtractSkillsTool tool = new ExtractSkillsTool(
                new SkillExtractor(new MockLLMClient("[\"Java\",\"Rust\"]")));
        String out = tool.execute(Map.of("text", "Java dev"));
        assertTrue(out.contains("Verified skills: Java"));
        assertTrue(out.contains("Unverified (removed): Rust"));
    }

    @Test
    void missingTextArgumentIsRejected() {
        ExtractSkillsTool tool = new ExtractSkillsTool(new SkillExtractor(new MockLLMClient()));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of()));
    }

    @Test
    void llmFailureBecomesIllegalState() {
        ExtractSkillsTool tool = new ExtractSkillsTool(new SkillExtractor(new MockLLMClient()));
        assertThrows(IllegalStateException.class, () -> tool.execute(Map.of("text", "Java dev")));
    }
}
