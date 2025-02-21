package com.davisellwood.website.dagger;

import com.davisellwood.website.dagger.implementations.InMemoryDatabase;
import com.davisellwood.website.dagger.implementations.SpacesObjectStore;
import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Module
@Slf4j
public class StorageModule {
    // TODO: Add dev resources for both of these as I don't want to accidentally corrupt livemode data
    @Provides
    @Singleton
    @Named("db-store")
    public ObjectStore provideDatabaseObjectStore() {
        String bucketKeyId = System.getenv("IN_MEM_DB_BUCKET_KEY_ID");
        String bucketKey = System.getenv("IN_MEM_DB_BUCKET_KEY");
        String bucketName = "davisellwood-website-in-memory-db";
        return new SpacesObjectStore(bucketKeyId, bucketKey, bucketName);
    }

    @Provides
    @Singleton
    @Named("static-store")
    public ObjectStore provideStaticObjectStore() {
        String bucketKeyId = System.getenv("STATIC_FILES_BUCKET_KEY_ID");
        String bucketKey = System.getenv("STATIC_FILES_BUCKET_KEY");

        log.info(bucketKeyId);

        String bucketName = "davisellwood-website";
        return new SpacesObjectStore(bucketKeyId, bucketKey, bucketName);
    }

    @Provides
    @Singleton
    @Inject
    public Database provideDatabase(@Named("db-store") ObjectStore objectStore) {
        return new InMemoryDatabase(objectStore);
    }
}
