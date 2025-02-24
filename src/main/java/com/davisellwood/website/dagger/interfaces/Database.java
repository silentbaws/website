package com.davisellwood.website.dagger.interfaces;

import com.google.protobuf.GeneratedMessage;
import java.util.List;
import javax.inject.Singleton;

@Singleton
public interface Database {
    <T extends GeneratedMessage> T get(Class<T> clazz, String collectionName, String keyName, String keyValue);
    
    <T extends GeneratedMessage> List<T> getAll(Class<T> clazz, String collectionName);

    // TODO: Add return type that indicates success/failure
    void put(String collectionName, GeneratedMessage value);

    void update(String collectionName, String keyName, String keyValue, GeneratedMessage value);
}
