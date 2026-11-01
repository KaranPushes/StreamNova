package com.streaming.mediaservice.exception;

import java.util.UUID;

public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(UUID mediaId) {
        super("Media not found with id: " + mediaId);
    }
}