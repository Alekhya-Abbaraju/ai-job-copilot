package com.alekhya.resume_service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfig {
    @Bean
    @ConditionalOnProperty(
            name = "app.resume.storage.type",
            havingValue = "s3"
    )
    public S3Client s3Client(
            @org.springframework.beans.factory.annotation.Value(
                    "${app.resume.storage.s3.region:ap-south-1}")
            String region) {
        return S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build();
    }
}
