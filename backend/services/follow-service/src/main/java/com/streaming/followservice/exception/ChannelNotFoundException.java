package com.streaming.followservice.exception;

public class ChannelNotFoundException extends RuntimeException {

    public ChannelNotFoundException() {
        super("Channel not found");
    }
}