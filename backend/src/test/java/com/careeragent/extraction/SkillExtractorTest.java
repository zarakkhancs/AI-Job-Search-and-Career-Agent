package com.careeragent.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.llm.MockLLMClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillExtractorTest {

    @Test
    void returnsVerifiedSkills() throws Exception {
        SkillExtractor ex = new SkillExtractor(new MockLLMClient("[\"Java\",\"SQL\"]"));
        ExtractionResult r = ex.extract("Java and SQL developer");
        assertEquals(List.of("Java", "SQL"), r.verified());
        assertTrue(r.rejected().isEmpty());
    }

    @Test
    void hallucinatedSkillIsRejected() throws Exception {
        SkillExtractor ex = new SkillExtractor(new MockLLMClient("[\"Java\",\"Kubernetes\"]"));
        ExtractionResult r = ex.extract("Java developer");
        assertEquals(List.of("Java"), r.verified());
        assertEquals(List.of("Kubernetes"), r.rejected());
    }

    @Test
    void toleratesTextAroundJson() throws Exception {
        SkillExtractor ex = new SkillExtractor(new MockLLMClient("Here you go: [\"Java\"] hope that helps"));
        assertEquals(List.of("Java"), ex.extract("Java developer").verified());
    }

    @Test
    void invalidResponseThrows() {
        SkillExtractor ex = new SkillExtractor(new MockLLMClient("no list here"));
        assertThrows(ExtractionException.class, () -> ex.extract("Java developer"));
    }

    @Test
    void blankTextRejectedWithoutCallingLlm() {
        MockLLMClient mock = new MockLLMClient("[\"Java\"]");
        SkillExtractor ex = new SkillExtractor(mock);
        assertThrows(IllegalArgumentException.class, () -> ex.extract("  "));
        assertTrue(mock.getPrompts().isEmpty());
    }
}
