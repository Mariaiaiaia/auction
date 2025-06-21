package com.maria.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3AsyncClient s3Client;

    public Mono<String> uploadDataToS3(String bucket, String key, byte[] file) {
        return Mono.fromFuture(s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType("image/jpeg")
                                .build(),
                        AsyncRequestBody.fromBytes(file)
                ))
                .thenReturn("https://" + bucket + ".s3.eu-north-1.amazonaws.com/" + key);
    }
}
