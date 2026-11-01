package com.streaming.streamservice.exception;

public class InvalidStreamKeyException extends RuntimeException {

    public InvalidStreamKeyException(String message) {
        super(message);
    }
}