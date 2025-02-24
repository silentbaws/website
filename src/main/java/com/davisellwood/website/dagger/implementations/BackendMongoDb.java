package com.davisellwood.website.dagger.implementations;

import static com.mongodb.client.model.Filters.eq;

import com.davisellwood.website.dagger.interfaces.Database;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.util.JsonFormat;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

@Slf4j
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class BackendMongoDb implements Database {
    
    private final MongoClient client;


    public BackendMongoDb(String password, String dbName, String url) {
        String connectionUrl = String.format("mongodb+srv://doadmin:%s@%s/%s?tls=true&authSource=admin",
                password,
                url,
                dbName);

        client = MongoClients.create(connectionUrl);
    }

    private <T> MongoCollection<T> getCollection(String collectionName, Class<T> clazz) {
        MongoDatabase database = client.getDatabase("davisellwood_backend");
        return database.getCollection(collectionName, clazz);
    }


    @Override
    public <T extends GeneratedMessage> T get(
            Class<T> clazz,
            String collectionName,
            String keyName,
            String keyValue) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName, Document.class);
            Document doc = collection.find(eq(keyName, keyValue)).first();

            if (doc == null) {
                return null;
            }

            GeneratedMessage.Builder builder = (GeneratedMessage.Builder) clazz.getMethod("newBuilder").invoke(null);

            JsonFormat.parser().ignoringUnknownFields().merge(doc.toJson(), builder);

            return (T) builder.build();
        } catch (Exception e) {
            log.error("Error getting db item {} from collection {}", keyName, collectionName, e);
            return null;
        }
    }

    @Override
    public <T extends GeneratedMessage> List<T> getAll(Class<T> clazz, String collectionName) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName, Document.class);
            MongoCursor<Document> allItems = collection.find().iterator();

            List<T> results = new ArrayList<>();

            while (allItems.hasNext()) {
                GeneratedMessage.Builder builder = (GeneratedMessage.Builder) clazz.getMethod("newBuilder")
                        .invoke(null);

                JsonFormat.parser().ignoringUnknownFields().merge(allItems.next().toJson(), builder);
                results.add((T) builder.build());
            }

            return results;
        } catch (Exception e) {
            log.error("Error getting db items from collection {}", collectionName, e);
            return List.of();
        }
    }

    @Override
    public void put(String collectionName, GeneratedMessage value) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName, Document.class);
            InsertOneResult result = collection.insertOne(Document.parse(JsonFormat.printer().print(value)));

            log.info("{}", result.toString());
        } catch (Exception e) {
            log.error("Error inserting db item {} to collection {}", value.toString(), collectionName, e);
        }
    }

    @Override
    public void update(
            String collectionName, 
            String keyName,
            String keyValue,
            GeneratedMessage value) {

        try {
            MongoCollection<Document> collection = getCollection(collectionName, Document.class);
            UpdateResult result = collection.replaceOne(
                    eq(keyName, keyValue),
                    Document.parse(JsonFormat.printer().print(value)));

            log.info("{}", result.toString());
        } catch (Exception e) {
            log.error("Error updating db item {} to collection {}", keyValue, collectionName, e);
        }
    }
    
}
