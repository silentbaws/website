package com.davisellwood.website.common;

import org.springframework.stereotype.Component;

@Component
public class SpringStorageProvider {
    private static StorageComponent storageComponent;

    static {
        storageComponent = DaggerStorageComponent.builder().build();
    }

    public Database database() {
        return storageComponent.provideDatabase();
    }
}
