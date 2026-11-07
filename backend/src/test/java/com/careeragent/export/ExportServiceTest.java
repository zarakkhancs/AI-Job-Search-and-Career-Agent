package com.careeragent.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportServiceTest {
    private final ExportService service = new ExportService();

    @Test
    void buildsAllSectionsInOrder() {
        String content = service.build("Acme", "Resume body", "Summary body", "Letter body", List.of()).content();

        assertTrue(content.startsWith("APPLICATION PACKAGE"));
        int summary = content.indexOf("SUMMARY");
        int letter = content.indexOf("COVER LETTER");
        int resume = content.indexOf("RESUME");
        assertTrue(summary > 0 && summary < letter && letter < resume);
        assertTrue(content.contains("Summary body"));
        assertTrue(content.contains("Letter body"));
        assertTrue(content.contains("Resume body"));
    }

    @Test
    void addsReviewBlockForFlaggedSkills() {
        String content = service.build("Acme", "R", "S", "C", List.of("Java", "Kafka")).content();

        assertTrue(content.contains("REVIEW BEFORE SENDING"));
        assertTrue(content.contains("Java, Kafka"));
    }

    @Test
    void omitsReviewBlockWhenNothingIsFlagged() {
        assertFalse(service.build("Acme", "R", "S", "C", List.of()).content().contains("REVIEW BEFORE SENDING"));
        assertFalse(service.build("Acme", "R", "S", "C", null).content().contains("REVIEW BEFORE SENDING"));
    }

    @Test
    void filenameIsASafeSlug() {
        assertEquals("application-package-acme-corp-sr-dev.txt",
                service.build("Acme Corp / Sr. Dev!!", "R", "S", "C", List.of()).filename());
        assertEquals("application-package.txt", service.build(null, "R", "S", "C", List.of()).filename());
        assertEquals("application-package.txt", service.build("  ", "R", "S", "C", List.of()).filename());
    }

    @Test
    void rejectsBlankParts() {
        assertThrows(IllegalArgumentException.class, () -> service.build("x", " ", "S", "C", List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.build("x", "R", null, "C", List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.build("x", "R", "S", "", List.of()));
    }

    @Test
    void saveWritesTheFileToTheDirectory(@TempDir Path dir) throws IOException {
        ExportFile file = service.build("Acme", "R", "S", "C", List.of());

        Path saved = service.save(file, dir.resolve("out"));

        assertTrue(Files.exists(saved));
        assertEquals(file.content(), Files.readString(saved));
    }
}