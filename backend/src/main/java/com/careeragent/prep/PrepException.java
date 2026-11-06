package com.careeragent.prep;

public class PrepException extends RuntimeException {
    public PrepException(String message) {
        super(message);
    }

    public PrepException(String message, Throwable cause) {
        super(message, cause);
    }
}