package com.davisellwood.website.common;

import javax.inject.Singleton;

@Singleton
public interface Database {
    String get(String key);
    void put(String key, String value);
}
