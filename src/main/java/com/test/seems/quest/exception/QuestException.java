package com.test.seems.quest.exception;

public class QuestException extends RuntimeException {

    public QuestException(String message) {
        super(message);
    }

    public QuestException(String message, Throwable cause) {
        super(message, cause);
    }
}
