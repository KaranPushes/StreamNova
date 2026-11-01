package com.streaming.mediaservice.storage;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface ObjectStorageService {

    String upload(
            String key,
            InputStream inputStream,
            long size,
            String contentType
    );

    Resource download(String key);

    void delete(String key);

}