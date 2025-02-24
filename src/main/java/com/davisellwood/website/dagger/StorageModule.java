package com.davisellwood.website.dagger;

import com.davisellwood.website.dagger.implementations.BackendMongoDb;
import com.davisellwood.website.dagger.implementations.SpacesObjectStore;
import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Module
@Slf4j
public class StorageModule {
    @Provides
    @Singleton
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
    public Database provideDatabase() {
        String dbName = "davisellwood_backend";
        String password = System.getenv("DB_PASSWORD");
        String url = System.getenv("DB_URL");

        return new BackendMongoDb(password, dbName, url);
    }
}
