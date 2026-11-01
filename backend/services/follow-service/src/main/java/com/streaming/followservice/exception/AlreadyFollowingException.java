package com.streaming.followservice.exception;

public class AlreadyFollowingException extends RuntimeException {

    public AlreadyFollowingException() {
        super("You are already following this channel");
    }
}