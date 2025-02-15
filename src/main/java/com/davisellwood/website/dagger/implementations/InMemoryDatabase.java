package com.davisellwood.website.dagger.implementations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.davisellwood.website.dagger.interfaces.Database;
import com.davisellwood.website.dagger.interfaces.ObjectStore;

import lombok.extern.slf4j.Slf4j;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase;
import proto.davisellwood.website.cheapskate.CheapSkateDatabase.Database.DBEntry;

@Slf4j
public class InMemoryDatabase implements Database {
    private static long SAVE_PERIOD = 15 * 1000;

    private final ObjectStore objectStore;

    private final ConcurrentHashMap<String, DBEntry> store;
    private final Timer saveTimer;

    private final Timer loadTimer;
    private boolean hasLoadedFromBucket = false;

    public InMemoryDatabase(ObjectStore objectStore) {
        log.info("Creating new in memory database");
        this.objectStore = objectStore;

        store = new ConcurrentHashMap<String, DBEntry>();

        this.loadTimer = new Timer();
        loadTimer.scheduleAtFixedRate(new LoadTask(), 1000 , 15000);

        saveTimer = new Timer();
        saveTimer.scheduleAtFixedRate(new SaveTask(), 10 * 1000, SAVE_PERIOD);
    }

    // TODO: remove
    private static String createRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return 
            random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    
    // TODO: Save to storage and read from storage for real
    private class SaveTask extends TimerTask {
        @Override
        public void run() {
            try {
                
            } catch (Exception e) {
                log.error("Error trying to save in memory DB to bucket", e);
            }
        }
    }

    private class LoadTask extends TimerTask {
        @Override
        public void run() {
            if (store == null) {
                log.error("STORE IS NULL");
                return;
            }

            if (hasLoadedFromBucket) {
                loadTimer.cancel();
                return;
            }

            try {
                var r = new FileInputStream("databaseBytes1739589145");
                CheapSkateDatabase.Database db = CheapSkateDatabase.Database.parseFrom(r);
                store.putAll(db.getEntriesMap());
                r.close();

                log.info("loaded db from disk");
                log.info(store.toString());
            } catch (FileNotFoundException e) {
            } catch (IOException e) { }
        }
    }

    @Override
    public String get(String key) {
        return "";
    }

    @Override
    public void put(String key, String value) {
        var key2 = createRandomString();
        var val = createRandomString();
        log.error(String.format("putting value %s in db with key %s", val, key2));
        store.put(key2, DBEntry.newBuilder().setStringValue(val).build());
    }
}
