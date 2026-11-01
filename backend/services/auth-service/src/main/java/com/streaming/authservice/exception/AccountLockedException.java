package com.streaming.authservice.exception;

public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("Account is temporarily locked. Please try again later.");
    }
}