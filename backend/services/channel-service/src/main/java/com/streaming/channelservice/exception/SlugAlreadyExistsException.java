package com.streaming.channelservice.exception;

public class SlugAlreadyExistsException extends RuntimeException {

    public SlugAlreadyExistsException(String slug) {
        super("Channel slug already exists: " + slug);
    }
}