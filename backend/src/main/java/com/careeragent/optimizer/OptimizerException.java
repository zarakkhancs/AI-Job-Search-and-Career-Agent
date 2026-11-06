package com.careeragent.optimizer;

public class OptimizerException extends RuntimeException {
    public OptimizerException(String message) {
        super(message);
    }

    public OptimizerException(String message, Throwable cause) {
        super(message, cause);
    }
}