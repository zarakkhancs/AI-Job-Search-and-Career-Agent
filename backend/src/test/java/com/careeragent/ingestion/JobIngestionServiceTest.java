package com.careeragent.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JobIngestionServiceTest {
    private static final String PAGE = "<html><head><style>body{color:red}</style></head><body>"
            + "<h1>Java Developer</h1><script>var secret = 1;</script>"
            + "<p>We need Java &amp; SQL skills for the backend team.</p><ul><li>REST APIs</li></ul></body></html>";

    private static JobIngestionService service() {
        return new JobIngestionService(url -> PAGE);
    }

    @Test
    void importsPastedText() {
        JobPosting posting = service().ingest("Backend developer needed. Java, SQL and REST experience required.");

        assertEquals(1, posting.id());
        assertEquals("pasted text", posting.source());
        assertTrue(posting.text().contains("Java, SQL and REST"));
    }

    @Test
    void importsAUrlAndStripsHtml() {
        JobPosting posting = service().ingest("https://example.com/jobs/42");

        assertEquals("https://example.com/jobs/42", posting.source());
        assertTrue(posting.text().contains("Java Developer"));
        assertTrue(posting.text().contains("Java & SQL skills for the backend team."));
        assertTrue(posting.text().contains("REST APIs"));
        assertFalse(posting.text().contains("secret"));
        assertFalse(posting.text().contains("color:red"));
        assertFalse(posting.text().contains("<"));
    }

    @Test
    void keepsAListAndLooksPostingsUpById() {
        JobIngestionService service = service();

        service.ingest("https://example.com/a");
        service.ingestText("job.txt", "Data analyst role. Python, SQL and Excel skills are needed.");

        assertEquals(2, service.list().size());
        assertEquals("job.txt", service.get(2).source());
        assertThrows(JobIngestException.class, () -> service.get(7));
    }

    @Test
    void rejectsBlankAndTooShortInput() {
        JobIngestionService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.ingest("   "));
        assertThrows(JobIngestException.class, () -> service.ingest("too short"));
        assertThrows(JobIngestException.class, () -> new JobIngestionService(url -> "<html></html>").ingest("https://example.com/empty"));
    }

    @Test
    void textThatLooksLikeAUrlButHasSpacesIsTreatedAsText() {
        JobPosting posting = service().ingest("http://example.com is where this job is posted, see Java details here");

        assertEquals("pasted text", posting.source());
    }
}