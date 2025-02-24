package com.davisellwood.website.dagger;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = StorageModule.class)
public interface StorageComponent {
    ObjectStore provideObjectStore();
    
    Database provideDatabase();
} 