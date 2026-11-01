package com.streaming.mediaservice.exception;

public class MediaSizeExceededException extends RuntimeException {

    public MediaSizeExceededException(
            long actualSize,
            long maximumSize) {

        super(
                "File size exceeds the maximum allowed size of "
                        + maximumSize
                        + " bytes"
        );
    }
}