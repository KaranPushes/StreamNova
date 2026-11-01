package com.streaming.channelservice.exception;

public class ChannelAlreadyExistsException extends RuntimeException {

    public ChannelAlreadyExistsException() {
        super("User already owns a channel");
    }
}