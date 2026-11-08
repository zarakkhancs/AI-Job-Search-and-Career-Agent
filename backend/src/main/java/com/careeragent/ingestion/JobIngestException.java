package com.careeragent.ingestion;

/** Raised when a job posting cannot be imported (bad URL, unreadable page, too little text). */
public class JobIngestException extends RuntimeException {
    public JobIngestException(String message) {
        super(message);
    }

    public JobIngestException(String message, Throwable cause) {
        super(message, cause);
    }
}