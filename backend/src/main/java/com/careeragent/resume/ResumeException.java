package com.careeragent.resume;

/** Raised when a resume cannot be imported (unreadable PDF, no text, too short). */
public class ResumeException extends RuntimeException {
    public ResumeException(String message) {
        super(message);
    }

    public ResumeException(String message, Throwable cause) {
        super(message, cause);
    }
}