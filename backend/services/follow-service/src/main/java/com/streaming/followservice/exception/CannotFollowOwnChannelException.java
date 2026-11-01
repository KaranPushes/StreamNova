package com.streaming.followservice.exception;

public class CannotFollowOwnChannelException extends RuntimeException {

    public CannotFollowOwnChannelException() {
        super("You cannot follow your own channel");
    }
}