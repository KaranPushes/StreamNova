package com.streaming.authservice.service;

import java.util.UUID;

public interface SessionValidationService {

    boolean isSessionValid(UUID sessionId);

}