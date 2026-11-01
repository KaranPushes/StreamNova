package com.streaming.mediaservice;

import com.streaming.mediaservice.config.JwtProperties;
import com.streaming.mediaservice.config.MediaProperties;
import com.streaming.mediaservice.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, MediaProperties.class, JwtProperties.class})
public class MediaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediaServiceApplication.class, args);
	}

}
