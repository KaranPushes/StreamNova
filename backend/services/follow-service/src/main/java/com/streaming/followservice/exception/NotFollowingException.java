package com.streaming.followservice.exception;

public class NotFollowingException extends RuntimeException {

    public NotFollowingException() {
        super("You are not following this channel");
    }
}