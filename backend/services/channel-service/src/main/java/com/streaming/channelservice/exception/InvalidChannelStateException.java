package com.streaming.channelservice.exception;

public class InvalidChannelStateException extends RuntimeException {

    public InvalidChannelStateException(String message) {
        super(message);
    }
}