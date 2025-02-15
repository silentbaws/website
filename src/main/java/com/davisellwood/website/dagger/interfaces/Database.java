package com.davisellwood.website.dagger.interfaces;

import javax.inject.Singleton;

import proto.davisellwood.website.cheapskate.CheapSkateDatabase.Database.DBEntry;

@Singleton
public interface Database {
    DBEntry get(String key);
    void put(String key, DBEntry value);
}
