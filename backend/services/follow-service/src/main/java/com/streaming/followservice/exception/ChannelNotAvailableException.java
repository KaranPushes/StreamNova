package com.streaming.followservice.exception;

public class ChannelNotAvailableException extends RuntimeException {

    public ChannelNotAvailableException() {
        super("Channel is not available for following");
    }
}