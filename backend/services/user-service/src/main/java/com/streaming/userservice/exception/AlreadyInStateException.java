package com.streaming.userservice.exception;

public class AlreadyInStateException extends RuntimeException {

    public AlreadyInStateException(String resource,
                                   String identifier,
                                   Object value,
                                   String state) {

        super(resource + " with " + identifier + " '" + value
                + "' is already in '" + state + "' state.");
    }
}