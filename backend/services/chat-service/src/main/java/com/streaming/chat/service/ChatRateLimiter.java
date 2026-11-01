package com.streaming.chat.service;


import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ChatRateLimiter {

    private static final int CAPACITY = 10;
    private static final int REFILL_TOKENS = 10;
    private static final long REFILL_PERIOD_SECONDS = 10;

    private final ConcurrentMap<UUID, UserBucket> buckets =
            new ConcurrentHashMap<>();

    public boolean allow(UUID userId) {
        UserBucket bucket = buckets.computeIfAbsent(
                userId,
                ignored -> new UserBucket()
        );

        return bucket.tryConsume();
    }

    private static class UserBucket {

        private int tokens = CAPACITY;
        private long lastRefill = System.currentTimeMillis();

        synchronized boolean tryConsume() {
            refill();

            if (tokens <= 0) {
                return false;
            }

            tokens--;
            return true;
        }

        private void refill() {
            long now = System.currentTimeMillis();

            if (now - lastRefill >= REFILL_PERIOD_SECONDS * 1000) {
                tokens = CAPACITY;
                lastRefill = now;
            }
        }
    }
}