package com.careeragent.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.LLMClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class TailoredDocumentServiceTest {
    private static final String RESUME = "Built REST services in Java and SQL at a campus startup.";
    private static final String JOB = "We need Java, SQL and Docker experience.";
    private static final String JOB_SKILLS = "[\"Java\",\"SQL\",\"Docker\"]";

    /** Answers the extraction prompt with skills and the writing prompt with the given document. */
    private static TailoredDocumentService service(String documentJson) {
        LLMClient llm = prompt -> prompt.startsWith("Extract") ? JOB_SKILLS : documentJson;
        return new TailoredDocumentService(llm, new SkillExtractor(llm));
    }

    @Test
    void flagsASkillTheResumeDoesNotContain() throws Exception {
        String doc = "{\"summary\":\"Java and SQL developer.\","
                + "\"coverLetter\":\"I have Java, SQL and Docker experience.\"}";
        TailoredDocument d = service(doc).tailor(RESUME, JOB);
        assertEquals(List.of("Java", "SQL"), d.supportedSkills());
        assertEquals(List.of("Docker"), d.flaggedSkills());
    }

    @Test
    void flagsNothingWhenOutputStaysWithinTheResume() throws Exception {
        String doc = "{\"summary\":\"Java and SQL developer.\","
                + "\"coverLetter\":\"I built REST services and want to learn more.\"}";
        TailoredDocument d = service(doc).tailor(RESUME, JOB);
        assertEquals(List.of("Java", "SQL"), d.supportedSkills());
        assertTrue(d.flaggedSkills().isEmpty());
    }

    @Test
    void rejectsResponseThatIsNotJson() {
        assertThrows(DocumentException.class, () -> service("no idea").tailor(RESUME, JOB));
    }

    @Test
    void rejectsResponseMissingACoverLetter() {
        assertThrows(DocumentException.class,
                () -> service("{\"summary\":\"Only a summary.\"}").tailor(RESUME, JOB));
    }

    @Test
    void rejectsBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> service("{}").tailor("  ", JOB));
        assertThrows(IllegalArgumentException.class, () -> service("{}").tailor(RESUME, ""));
    }
}