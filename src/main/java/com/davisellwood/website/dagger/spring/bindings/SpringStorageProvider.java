package com.davisellwood.website.dagger.spring.bindings;

import org.springframework.stereotype.Component;

import com.davisellwood.website.dagger.DaggerStorageComponent;
import com.davisellwood.website.dagger.StorageComponent;
import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;

@Component
public class SpringStorageProvider {
    private static StorageComponent storageComponent;

    static {
        storageComponent = DaggerStorageComponent.builder().build();
    }

    public Database database() {
        return storageComponent.provideDatabase();
    }

    public ObjectStore objectStore() {
        return storageComponent.provideObjectStore();
    }
}
