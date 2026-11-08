package com.careeragent.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.careeragent.ingestion.JobIngestionService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JobCommandTest {
    private static JobCommand command() {
        return new JobCommand(new JobIngestionService(
                url -> "<html><body><p>Java developer needed for our backend team in Toronto.</p></body></html>"));
    }

    @Test
    void addsAJobFromAUrlThenListsAndShowsIt() {
        JobCommand cmd = command();

        String added = cmd.execute(List.of("add", "https://example.com/job"));
        assertTrue(added.startsWith("Imported job posting #1 (https://example.com/job)"));

        assertTrue(cmd.execute(List.of("list")).contains("#1 https://example.com/job"));
        assertTrue(cmd.execute(List.of("show", "1")).contains("Java developer needed"));
    }

    @Test
    void addsAJobFromATextFile(@TempDir Path temp) throws IOException {
        Path file = temp.resolve("job.txt");
        Files.writeString(file, "Data analyst role. Python, SQL and Excel skills are needed.");

        String out = command().execute(List.of("add", file.toString()));

        assertTrue(out.startsWith("Imported job posting #1"));
        assertTrue(out.contains("Python, SQL and Excel"));
    }

    @Test
    void reportsProblemsInsteadOfThrowing() {
        JobCommand cmd = command();

        assertEquals(JobCommand.USAGE, cmd.execute(List.of()));
        assertEquals(JobCommand.USAGE, cmd.execute(List.of("add")));
        assertEquals("No job postings imported yet.", cmd.execute(List.of("list")));
        assertTrue(cmd.execute(List.of("show", "9")).startsWith("Could not import the job posting"));
        assertEquals("The id must be a whole number.", cmd.execute(List.of("show", "abc")));
        assertTrue(cmd.execute(List.of("add", "no-such-file.txt")).startsWith("Could not read file"));
    }
}