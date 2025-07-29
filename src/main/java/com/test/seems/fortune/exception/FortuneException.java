package com.test.seems.fortune.exception;

public class FortuneException extends RuntimeException {
    
    public FortuneException(String message) {
        super(message);
    }
    
    public FortuneException(String message, Throwable cause) {
        super(message, cause);
    }
} 