package com.streaming.feed.exception;

public class StreamNotLiveException
        extends RuntimeException {

    public StreamNotLiveException(String message) {
        super(message);
    }
}