package com.davisellwood.website.dagger.interfaces;

import javax.inject.Singleton;

@Singleton
public interface Database {
    String get(String key);
    void put(String key, String value);
}
