package com.careeragent.ingestion;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HttpPageFetcherTest {
    @Test
    void refusesOtherSchemesAndBadUrls() {
        assertThrows(JobIngestException.class, () -> HttpPageFetcher.checkUrl("ftp://example.com/job"));
        assertThrows(JobIngestException.class, () -> HttpPageFetcher.checkUrl("file:///etc/passwd"));
        assertThrows(JobIngestException.class, () -> HttpPageFetcher.checkUrl("not a url"));
    }

    @Test
    void refusesLocalAndPrivateAddresses() {
        assertThrows(JobIngestException.class, () -> HttpPageFetcher.checkUrl("http://localhost:8080/job"));
        assertThrows(JobIngestException.class, () -> HttpPageFetcher.checkUrl("http://127.0.0.1/job"));
        assertThrows(JobIngestException.class, () -> HttpPageFetcher.checkUrl("http://192.168.1.10/job"));
        assertThrows(JobIngestException.class, () -> HttpPageFetcher.checkUrl("http://10.0.0.5/job"));
    }
}