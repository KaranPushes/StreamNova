package com.streaming.authservice.service.impl;

import com.streaming.authservice.service.TimeProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }

}