package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.documents.TailoredDocumentService;
import com.careeragent.export.ExportService;
import com.careeragent.extraction.SkillExtractor;
import com.careeragent.llm.LLMClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportCommandTest {
    private static final String DOC_JSON =
            "{\"summary\": \"Backend developer.\", \"coverLetter\": \"I would enjoy working with Java at Acme.\"}";

    private static ExportCommand command(Path outputDir) {
        LLMClient llm = prompt -> prompt.contains("cover letter") ? DOC_JSON : "[\"Java\"]";
        SkillExtractor extractor = new SkillExtractor(llm);
        return new ExportCommand(new TailoredDocumentService(llm, extractor), new ExportService(), outputDir);
    }

    @Test
    void tailorsSavesAndWarnsAboutSkillsMissingFromTheResume(@TempDir Path temp) throws IOException {
        Path resume = temp.resolve("resume.txt");
        Path job = temp.resolve("job.txt");
        Files.writeString(resume, "Python developer with three years of experience.");
        Files.writeString(job, "Java developer needed.");
        Path outDir = temp.resolve("exports");

        String out = command(outDir).execute(List.of(resume.toString(), job.toString(), "Acme"));

        Path saved = outDir.resolve("application-package-acme.txt");
        assertTrue(Files.exists(saved));
        assertTrue(out.contains("Saved application package"));
        assertTrue(out.contains("Warning: not found in your resume: Java"));
        String content = Files.readString(saved);
        assertTrue(content.contains("REVIEW BEFORE SENDING"));
        assertTrue(content.contains("Python developer"));
    }

    @Test
    void showsUsageWithoutArguments(@TempDir Path temp) {
        assertEquals(ExportCommand.USAGE, command(temp).execute(List.of()));
    }

    @Test
    void reportsUnreadableFiles(@TempDir Path temp) {
        String out = command(temp).execute(List.of("no-such-resume.txt", "no-such-job.txt"));

        assertTrue(out.startsWith("Could not read file"));
    }
}