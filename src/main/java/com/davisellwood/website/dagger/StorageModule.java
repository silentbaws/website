package com.davisellwood.website.dagger;

import javax.inject.Singleton;

import com.davisellwood.website.dagger.implementations.InMemoryDatabase;
import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;

import dagger.Module;
import dagger.Provides;

@Module
public class StorageModule {
    @Provides
    @Singleton
    public Database provideDatabase() {
        return new InMemoryDatabase();
    }

    @Provides
    @Singleton
    public ObjectStore provideObjectStore() {
        return null;
    }
}
