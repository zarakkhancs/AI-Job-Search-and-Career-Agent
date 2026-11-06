package com.careeragent.prompts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptFactoryTest {
    @Test
    void bulletFactoryBuildsBulletPrompt() {
        Prompt p = new BulletPromptFactory().create(Map.of("bullet", "Built a REST API"));
        assertTrue(p instanceof BulletOptimizerPrompt);
        assertTrue(p.text().contains("Built a REST API"));
        assertFalse(p.text().contains("Job posting:"));
    }

    @Test
    void bulletPromptIncludesJobWhenGiven() {
        Prompt p = new BulletPromptFactory().create(Map.of("bullet", "Built an API", "job", "Java developer"));
        assertTrue(p.text().contains("Job posting:"));
        assertTrue(p.text().contains("Java developer"));
    }

    @Test
    void prepFactoryBuildsPrepPromptWithOptionalResume() {
        Prompt p = new PrepSheetPromptFactory().create(Map.of("job", "Backend intern", "resume", "Java, SQL"));
        assertTrue(p instanceof PrepSheetPrompt);
        assertTrue(p.text().contains("Backend intern"));
        assertTrue(p.text().contains("Resume:"));
        Prompt noResume = new PrepSheetPromptFactory().create(Map.of("job", "Backend intern"));
        assertFalse(noResume.text().contains("Resume:"));
    }

    @Test
    void missingRequiredInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BulletPromptFactory().create(Map.of("job", "x")));
        assertThrows(IllegalArgumentException.class, () -> new PrepSheetPromptFactory().create(Map.of("job", "  ")));
    }
}