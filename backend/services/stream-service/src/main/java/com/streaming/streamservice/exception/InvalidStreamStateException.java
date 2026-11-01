package com.streaming.streamservice.exception;

public class InvalidStreamStateException extends RuntimeException {

    public InvalidStreamStateException(String message) {
        super(message);
    }
}