package com.davisellwood.website.dagger.implementations;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.davisellwood.website.dagger.interfaces.ObjectStore;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
public class SpacesObjectStore implements ObjectStore {
    private final S3Client client;

    private final String bucketKeyId;
    private final String bucketKey;
    private final String bucketName;

    // TODO: Inject this and update access keys to be for both buckets
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

    // TODO: Probably update Optional response to Either<Error, Response> and log from the caller
    @Override
    public Optional<byte[]> get(String key) {
        try {
            var resp = client.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build());
    
            return Optional.of(resp.readAllBytes());
        } catch (Exception e) {
            log.warn("Error trying to read object {} from bucket {}: ", key, bucketName, e);
            return Optional.empty();
        }
    }

    private boolean put(String key, byte[] value, boolean isPublic) {
        try {
            var resp = client.putObject(PutObjectRequest.builder().key(key).acl(isPublic ? "public-read" : "private").bucket(bucketName).build(), RequestBody.fromBytes(value));

            return resp.sdkHttpResponse().statusCode() == HttpStatus.OK.value();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean put(String key, byte[] value) {
        return put(key, value, false);
    }

    @Override
    public boolean putPublic(String key, byte[] value) {
        return put(key, value, true);
    }

    @Override
    public Optional<List<S3Object>> listObjects() {
        try {
            var resp = client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());

            return Optional.of(resp.contents());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
}
