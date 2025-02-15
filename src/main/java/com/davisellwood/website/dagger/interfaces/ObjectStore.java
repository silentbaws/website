package com.davisellwood.website.dagger.interfaces;

import java.util.Optional;
import javax.inject.Singleton;

@Singleton
public interface ObjectStore {
    boolean put(String key, byte[] value);
    Optional<byte[]> get(String key);
}
