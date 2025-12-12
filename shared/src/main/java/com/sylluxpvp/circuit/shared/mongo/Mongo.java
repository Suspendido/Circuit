package com.sylluxpvp.circuit.shared.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.connection.ConnectionPoolSettings;
import lombok.Getter;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Mongo {

    private static Mongo instance;
    private final MongoClient mongoClient;
    @Getter
    private final MongoDatabase database;

    private static final int MIN_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 50;
    private static final int MAX_WAIT_TIME_MS = 5000;
    private static final int MAX_CONNECTION_IDLE_TIME_MS = 60000;

    private Mongo(String mongoHost, int mongoPort, String databaseName) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(mongoHost, mongoPort))))
                .applyToConnectionPoolSettings(this::configureConnectionPool)
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName);
        createIndexes();
    }

    private Mongo(String mongoHost, int mongoPort, String username, char[] password, String authSource, String databaseName) {
        MongoCredential credential = MongoCredential.createCredential(username, authSource, password);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(mongoHost, mongoPort))))
                .applyToConnectionPoolSettings(this::configureConnectionPool)
                .credential(credential)
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName);
        createIndexes();
    }

    private void configureConnectionPool(ConnectionPoolSettings.Builder builder) {
        builder.minSize(MIN_POOL_SIZE)
                .maxSize(MAX_POOL_SIZE)
                .maxWaitTime(MAX_WAIT_TIME_MS, TimeUnit.MILLISECONDS)
                .maxConnectionIdleTime(MAX_CONNECTION_IDLE_TIME_MS, TimeUnit.MILLISECONDS);
    }

    private void createIndexes() {
        try {
            database.getCollection("profiles").createIndex(
                    Indexes.ascending("uuid"),
                    new IndexOptions().unique(true).background(true)
            );
            database.getCollection("profiles").createIndex(
                    Indexes.ascending("name"),
                    new IndexOptions().background(true)
            );
            database.getCollection("profiles").createIndex(
                    Indexes.ascending("address"),
                    new IndexOptions().background(true)
            );
            database.getCollection("ranks").createIndex(
                    Indexes.ascending("uuid"),
                    new IndexOptions().unique(true).background(true)
            );
            database.getCollection("ranks").createIndex(
                    Indexes.ascending("name"),
                    new IndexOptions().background(true)
            );
            database.getCollection("gifts").createIndex(
                    Indexes.ascending("uuid"),
                    new IndexOptions().unique(true).background(true)
            );
            database.getCollection("gifts").createIndex(
                    Indexes.ascending("code"),
                    new IndexOptions().unique(true).background(true)
            );
        } catch (Exception ignored) {
        }
    }

    public static synchronized Mongo getInstance(String mongoHost, int mongoPort, String databaseName) {
        if (instance == null) {
            instance = new Mongo(mongoHost, mongoPort, databaseName);
        }
        return instance;
    }

    public static synchronized Mongo getInstance(String mongoHost, int mongoPort, String username, char[] password, String authSource, String databaseName) {
        if (instance == null) {
            instance = new Mongo(mongoHost, mongoPort, username, password, authSource, databaseName);
        }
        return instance;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        instance = null;
    }
}