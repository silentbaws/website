package com.davisellwood.website.dagger.interfaces;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.services.s3.model.S3Object;

@Singleton
public interface ObjectStore {
    boolean put(String key, byte[] value);
    
    boolean putPublic(String key, byte[] value);

    Optional<byte[]> get(String key);

    Optional<List<S3Object>> listObjects();
}
