package com.streaming.mediaservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.storage")
public class StorageProperties {

    private String type = "local";

    private String basePath = "./storage";

    private String baseUrl = "http://localhost:8085/media/files";
}