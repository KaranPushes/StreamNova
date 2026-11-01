package com.streaming.authservice.service;

public interface TokenHashService {

    String hash(String rawToken);
}