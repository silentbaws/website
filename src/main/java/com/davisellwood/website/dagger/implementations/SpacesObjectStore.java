package com.davisellwood.website.dagger.implementations;

import java.net.URI;

import com.davisellwood.website.dagger.interfaces.ObjectStore;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Slf4j
public class SpacesObjectStore implements ObjectStore {
    private final S3Client client;

    private final String bucketKeyId;
    private final String bucketKey;
    private final String bucketName;

    private class SpacesCredentialProvider implements AwsCredentialsProvider {
        @Override
        public AwsCredentials resolveCredentials() {
            log.info("Resolving spaces credentials for bucket {}", bucketName);
            return AwsBasicCredentials.builder().accessKeyId(bucketKeyId).secretAccessKey(bucketKey).build();
        }
    }


    public SpacesObjectStore (String bucketKeyId, String bucketKey, String bucketName) {
        log.info("Creating S3 Client for bucket {}", bucketName);

        this.bucketKey = bucketKey;
        this.bucketKeyId = bucketKeyId;
        this.bucketName = bucketName;

        client = 
            S3Client.builder()
                .forcePathStyle(false)
                .endpointOverride(URI.create("https://nyc3.digitaloceanspaces.com"))
                .region(Region.US_EAST_1).credentialsProvider(new SpacesCredentialProvider())
                .httpClient(
                    ApacheHttpClient.builder()
                        .maxConnections(20)
                        .build())
                .build();
    }

    @Override
    // TODO: Update definition
    public String get(String key) {
        var resp = client.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build());

        return "";
    }
    
}
