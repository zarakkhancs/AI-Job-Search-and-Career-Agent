package com.careeragent.extraction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SourceVerifierTest {

    @Test
    void matchesCaseInsensitively() {
        assertTrue(SourceVerifier.isSupported("java", "Experienced in JAVA and SQL"));
    }

    @Test
    void doesNotMatchInsideLongerWord() {
        assertFalse(SourceVerifier.isSupported("Java", "I know JavaScript"));
    }

    @Test
    void matchesSymbolSkills() {
        assertTrue(SourceVerifier.isSupported("C++", "Worked with C++, Python"));
    }

    @Test
    void plainCDoesNotMatchCPlusPlus() {
        assertFalse(SourceVerifier.isSupported("C", "Worked with C++"));
    }

    @Test
    void blankOrNullClaimIsUnsupported() {
        assertFalse(SourceVerifier.isSupported("", "Java"));
        assertFalse(SourceVerifier.isSupported(null, "Java"));
    }

    @Test
    void absentSkillIsUnsupported() {
        assertFalse(SourceVerifier.isSupported("Kubernetes", "Java and SQL"));
    }
}
