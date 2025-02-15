package com.davisellwood.website.common;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StorageModule {
    @Provides
    @Singleton
    public Database provideDatabase() {
        return new InMemoryDatabase();
    }
}
