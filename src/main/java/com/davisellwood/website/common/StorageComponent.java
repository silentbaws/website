package com.davisellwood.website.common;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = StorageModule.class)
public interface StorageComponent {
    Database provideDatabase();
} 