package com.streaming.mediaservice.storage;

import com.streaming.mediaservice.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class LocalObjectStorageService implements ObjectStorageService {

    private final StorageProperties storageProperties;

    @Override
    public String upload(
            String key,
            InputStream inputStream,
            long size,
            String contentType) {

        try {
            Path basePath =
                    Path.of(storageProperties.getBasePath())
                            .toAbsolutePath()
                            .normalize();

            Path target =
                    basePath.resolve(key)
                            .normalize();

            if (!target.startsWith(basePath)) {
                throw new IllegalArgumentException(
                        "Invalid storage key"
                );
            }

            Files.createDirectories(target.getParent());

            Files.copy(
                    inputStream,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return storageProperties.getBaseUrl()
                    + "/"
                    + key;

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to store media file",
                    exception
            );
        }
    }

    @Override
    public Resource download(String key) {

        try {

            Path basePath =
                    Path.of(storageProperties.getBasePath())
                            .toAbsolutePath()
                            .normalize();

            Path target =
                    basePath.resolve(key)
                            .normalize();

            if (!target.startsWith(basePath)) {
                throw new IllegalArgumentException(
                        "Invalid storage key"
                );
            }

            Resource resource =
                    new UrlResource(target.toUri());

            if (!resource.exists() ||
                    !resource.isReadable()) {

                throw new IllegalStateException(
                        "Media file not found"
                );
            }

            return resource;

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to read media file",
                    exception
            );
        }
    }

    @Override
    public void delete(String key) {

        try {
            Path basePath =
                    Path.of(storageProperties.getBasePath())
                            .toAbsolutePath()
                            .normalize();

            Path target =
                    basePath.resolve(key)
                            .normalize();

            if (!target.startsWith(basePath)) {
                throw new IllegalArgumentException(
                        "Invalid storage key"
                );
            }

            Files.deleteIfExists(target);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to delete media file",
                    exception
            );
        }
    }
}