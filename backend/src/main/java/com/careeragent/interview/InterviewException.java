package com.careeragent.interview;

public class InterviewException extends RuntimeException {
    public InterviewException(String message) {
        super(message);
    }

    public InterviewException(String message, Throwable cause) {
        super(message, cause);
    }
}