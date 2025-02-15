package com.davisellwood.website.dagger.interfaces;

import javax.inject.Singleton;

@Singleton
public interface ObjectStore {
    String get(String key);
}
