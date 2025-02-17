package com.davisellwood.website.dagger.implementations;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.extern.slf4j.Slf4j;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase.Database.DBEntry;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
public class InMemoryDatabase implements Database {

    private final Timer LOAD_TIMER;

    private final ObjectStore objectStore;

    private final ConcurrentHashMap<String, DBEntry> database;
    private boolean hasLoadedFromBucket = false;

    public InMemoryDatabase(ObjectStore objectStore) {
        log.info("Creating new in memory database");

        this.objectStore = objectStore;

        database = new ConcurrentHashMap<String, DBEntry>();

        LOAD_TIMER = new Timer();
        LOAD_TIMER.scheduleAtFixedRate(new LoadTask(), 1000 , 5000);
    }

    private static String createObjectKeyFromDate() {
        ZonedDateTime currentTime = Instant.now().atZone(ZoneOffset.UTC);
        return String.format("database-backup-%s-%s-%s", currentTime.getYear(), currentTime.getMonthValue(), currentTime.getDayOfMonth());
    }

    private void save() {
        log.info("Attempting to save database to bucket");
        if (!hasLoadedFromBucket) {
            log.warn("Attempting to save database before load");
            return;
        }

        String key = createObjectKeyFromDate();
        boolean success = objectStore.put(key, CheapSkateDatabase.Database.newBuilder().putAllEntries(database).build().toByteArray());
        if (!success) {
            log.error("Failed to save database to bucket");
        }
    }

    private class LoadTask extends TimerTask {
        @Override
        public void run() {
            if (database == null) {
                log.error("Database was null during load");
                return;
            }

            if (hasLoadedFromBucket) {
                LOAD_TIMER.cancel();
                return;
            }

            Optional<List<S3Object>> savedDatabases = objectStore.listObjects();
            if (savedDatabases.isEmpty() || savedDatabases.get().isEmpty()) {
                log.error("Error listing database bucket");
            }

            log.debug("Iterating over previously saved databases");
            S3Object mostRecentDatabaseBackup = null;
            for (S3Object savedDatabase : savedDatabases.get()) {
                log.debug("Previous databse {}, last modified {}", savedDatabase.key(), savedDatabase.lastModified().toString());

                if (mostRecentDatabaseBackup == null || mostRecentDatabaseBackup.lastModified().toEpochMilli() < savedDatabase.lastModified().toEpochMilli()) {
                    log.debug("Updating most recent with current");
                    mostRecentDatabaseBackup = savedDatabase;
                }
            }

            Optional<byte[]> databaseBytes = objectStore.get(mostRecentDatabaseBackup.key());
            if (databaseBytes.isPresent()) {
                CheapSkateDatabase.Database dbProto;
                try {
                    dbProto = CheapSkateDatabase.Database.parseFrom(databaseBytes.get());
                    database.putAll(dbProto.getEntriesMap());

                    hasLoadedFromBucket = true;

                    log.info("Successfully loaded database from disk {}", database.toString());
                } catch (InvalidProtocolBufferException e) {
                    log.error("Error parsing database from disk: ", e);
                }
            } else {
                log.error("Error loading database from disk");
            }
        }
    }

    @Override
    public DBEntry get(String key) {
        if (database == null) {
            return null;
        }

        return database.get(key);
    }

    @Override
    public void put(String key, DBEntry value) {
        if (database == null) {
            return;
        }

        database.put(key, value);
        save();
    }
}
