package com.maria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
public class S3Config {
    @Bean
    public S3AsyncClient s3Client() {
        return S3AsyncClient.builder()
                .region(Region.EU_NORTH_1)
                .build();
    }
}
