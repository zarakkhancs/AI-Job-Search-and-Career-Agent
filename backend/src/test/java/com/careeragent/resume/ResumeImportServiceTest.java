package com.careeragent.resume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.LLMClient;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ResumeImportServiceTest {
    private static ResumeImportService service() {
        LLMClient llm = prompt -> "[\"Java\",\"SQL\",\"Rust\"]";
        return new ResumeImportService(new PdfTextExtractor(), new SkillExtractor(llm));
    }

    @Test
    void importsAPdfAndVerifiesSkillsAgainstItsText() throws Exception {
        byte[] pdf = TestPdf.bytes("Java and SQL developer with five years of experience.");

        ParsedResume parsed = service().importPdf("resume.pdf", pdf);

        assertEquals(1, parsed.id());
        assertEquals("resume.pdf", parsed.filename());
        assertTrue(parsed.text().contains("Java and SQL developer"));
        assertEquals(java.util.List.of("Java", "SQL"), parsed.skills());
        assertEquals(java.util.List.of("Rust"), parsed.rejectedSkills());
    }

    @Test
    void rejectsAPdfWithNoSelectableText() throws IOException {
        ResumeException e = assertThrows(ResumeException.class,
                () -> service().importPdf("blank.pdf", TestPdf.bytes()));

        assertTrue(e.getMessage().contains("No selectable text"));
    }

    @Test
    void rejectsFilesThatAreNotPdfs() {
        assertThrows(ResumeException.class,
                () -> service().importPdf("fake.pdf", "this is not a pdf".getBytes()));
        assertThrows(ResumeException.class, () -> service().importPdf("empty.pdf", new byte[0]));
    }

    @Test
    void importsPastedTextAndKeepsAList() throws Exception {
        ResumeImportService service = service();

        service.importText("first", "Java and SQL developer with five years of experience.");
        ParsedResume second = service.importText(null, "Another resume that mentions Java and SQL work.");

        assertEquals(2, second.id());
        assertEquals("resume", second.filename());
        assertEquals(2, service.list().size());
        assertEquals("first", service.get(1).filename());
    }

    @Test
    void rejectsBlankOrTooShortText() {
        ResumeImportService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.importText("x", "  "));
        assertThrows(ResumeException.class, () -> service.importText("x", "Java"));
        assertThrows(ResumeException.class, () -> service.get(99));
    }
}