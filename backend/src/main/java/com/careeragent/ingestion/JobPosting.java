package com.careeragent.ingestion;

/** An imported job posting. The source is the URL, or "pasted text" / a file name. */
public record JobPosting(int id, String source, String text) {
}