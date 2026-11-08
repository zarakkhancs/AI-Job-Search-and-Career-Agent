package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.LLMClient;
import com.careeragent.resume.PdfTextExtractor;
import com.careeragent.resume.ResumeImportService;
import com.careeragent.resume.TestPdf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResumeCommandTest {
    private static ResumeCommand command() {
        LLMClient llm = prompt -> "[\"Java\",\"SQL\",\"Rust\"]";
        return new ResumeCommand(new ResumeImportService(new PdfTextExtractor(), new SkillExtractor(llm)));
    }

    @Test
    void importsAPdfListsAndShowsIt(@TempDir Path temp) throws IOException {
        Path pdf = temp.resolve("resume.pdf");
        Files.write(pdf, TestPdf.bytes("Java and SQL developer with five years of experience."));
        ResumeCommand cmd = command();

        String out = cmd.execute(List.of("import", pdf.toString()));

        assertTrue(out.startsWith("Imported resume #1 (resume.pdf)"));
        assertTrue(out.contains("Skills: Java, SQL"));
        assertTrue(out.contains("Rejected (not found in the text): Rust"));
        assertTrue(cmd.execute(List.of("list")).contains("#1 resume.pdf (2 skills)"));
        assertTrue(cmd.execute(List.of("show", "1")).contains("Skills: Java, SQL"));
    }

    @Test
    void importsATextFile(@TempDir Path temp) throws IOException {
        Path txt = temp.resolve("resume.txt");
        Files.writeString(txt, "Java and SQL developer with five years of experience.");

        String out = command().execute(List.of("import", txt.toString()));

        assertTrue(out.startsWith("Imported resume #1 (resume.txt)"));
    }

    @Test
    void reportsProblemsInsteadOfThrowing(@TempDir Path temp) throws IOException {
        ResumeCommand cmd = command();
        Path bad = temp.resolve("bad.pdf");
        Files.writeString(bad, "this is not a pdf");

        assertEquals(ResumeCommand.USAGE, cmd.execute(List.of()));
        assertEquals(ResumeCommand.USAGE, cmd.execute(List.of("import")));
        assertEquals("No resumes imported yet.", cmd.execute(List.of("list")));
        assertTrue(cmd.execute(List.of("import", "missing.pdf")).startsWith("Could not read file"));
        assertTrue(cmd.execute(List.of("import", bad.toString())).startsWith("Could not import the resume"));
        assertTrue(cmd.execute(List.of("show", "9")).contains("No resume with id 9"));
    }
}