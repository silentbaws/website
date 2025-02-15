package com.davisellwood.website.dagger;

import javax.inject.Named;
import javax.inject.Singleton;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;

import dagger.Component;

@Singleton
@Component(modules = StorageModule.class)
public interface StorageComponent {
    Database provideDatabase();
    @Named("static-store") ObjectStore provideObjectStore();
} 