package com.streaming.mediaservice.exception;

public class UnsupportedMediaTypeException extends RuntimeException {

    public UnsupportedMediaTypeException(String contentType) {
        super("Unsupported media type: " + contentType);
    }
}