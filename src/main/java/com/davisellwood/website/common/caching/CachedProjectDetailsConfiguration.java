package com.davisellwood.website.common.caching;

import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

@Component
public class CachedProjectDetailsConfiguration implements CachingConfigurer {

    @Value("${com.davisellwood.website.work.cache.duration.minutes}") int workProjectCacheMinutes = 0;

    @Override
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(final String name) {
                return new ConcurrentMapCache(
                        name,
                        CacheBuilder.newBuilder()
                                .expireAfterWrite(workProjectCacheMinutes, TimeUnit.MINUTES)
                                .maximumSize(100)
                                .build()
                                .asMap(),
                        false
                );
            }
        };
    }
}
