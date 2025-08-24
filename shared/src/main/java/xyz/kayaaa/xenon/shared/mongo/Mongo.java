package xyz.kayaaa.xenon.shared.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import xyz.kayaaa.xenon.shared.XenonShared;

import java.util.Collections;

public class Mongo {

    private static Mongo instance;
    private final MongoClient mongoClient;
    @Getter
    private final MongoDatabase database;

    private Mongo(String mongoHost, int mongoPort, String databaseName) {
        MongoClientSettings settings = MongoClientSettings.builder().applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(mongoHost, mongoPort)))).build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName);
    }

    private Mongo(String mongoHost, int mongoPort, String username, char[] password, String authSource, String databaseName) {
        MongoCredential credential = MongoCredential.createCredential(username, authSource, password);
        MongoClientSettings settings = MongoClientSettings.builder().applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(mongoHost, mongoPort)))).credential(credential).build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName);
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
            XenonShared.getInstance().getLogger().log(true, "Shutting down MongoDB connection...");
            mongoClient.close();
        }
        instance = null;
    }
}