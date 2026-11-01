package com.streaming.authservice.service;

import java.time.Instant;

public interface TimeProvider {

    Instant now();

}