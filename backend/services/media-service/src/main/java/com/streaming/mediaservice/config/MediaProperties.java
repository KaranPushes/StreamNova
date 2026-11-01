package com.streaming.mediaservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.media")
public class MediaProperties {

    private Limits limits = new Limits();

    private List<String> allowedImageTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Getter
    @Setter
    public static class Limits {

        private DataSize avatarMaxSize = DataSize.ofMegabytes(5);

        private DataSize bannerMaxSize = DataSize.ofMegabytes(10);

        private DataSize streamThumbnailMaxSize =
                DataSize.ofMegabytes(10);
    }
}