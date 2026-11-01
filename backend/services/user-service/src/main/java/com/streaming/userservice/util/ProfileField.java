package com.streaming.userservice.util;

import java.util.function.Function;

public record ProfileField(
        String name,
        Function<com.streaming.userservice.entity.User, String> extractor
) {
}