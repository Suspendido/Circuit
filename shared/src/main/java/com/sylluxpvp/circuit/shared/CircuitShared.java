package com.sylluxpvp.circuit.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bson.Document;
import redis.clients.jedis.JedisPubSub;
import com.sylluxpvp.circuit.shared.grant.Grant;
import com.sylluxpvp.circuit.shared.mongo.Mongo;
import com.sylluxpvp.circuit.shared.credentials.MongoCredentials;
import com.sylluxpvp.circuit.shared.redis.Redis;
import com.sylluxpvp.circuit.shared.credentials.RedisCredentials;
import com.sylluxpvp.circuit.shared.redis.RedisPacketRegistry;
import com.sylluxpvp.circuit.shared.redis.listener.ServerUpdateListener;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerUpdatePacket;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;
import com.sylluxpvp.circuit.shared.tools.gson.GrantAdapter;
import com.sylluxpvp.circuit.shared.tools.gson.UUIDAdapter;
import com.sylluxpvp.circuit.shared.tools.circuit.CircuitLogger;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Getter
public class CircuitShared {

    @Getter private static CircuitShared instance;
    private final Gson gson;
    private final Redis redis;
    private final Mongo mongo;

    @Getter @Setter private CircuitLogger logger;
    @Getter private Server server;

    public CircuitShared(CircuitLogger logger, RedisCredentials redisCredentials, MongoCredentials mongoCredentials, String databaseName) {
        instance = this;
        this.logger = logger;

        if (mongoCredentials.getUsername() != null && !mongoCredentials.getUsername().isEmpty()) {
            this.mongo = Mongo.getInstance(mongoCredentials.getHostname(), mongoCredentials.getPort(),
                    mongoCredentials.getUsername(), mongoCredentials.getPassword().toCharArray(),
                    mongoCredentials.getAuthSource(), databaseName);
        } else {
            this.mongo = Mongo.getInstance(mongoCredentials.getHostname(), mongoCredentials.getPort(), databaseName);
        }

        if (redisCredentials.getPassword() != null && !redisCredentials.getPassword().isEmpty()) {
            this.redis = Redis.getInstance(redisCredentials.getHostname(), redisCredentials.getPort(), redisCredentials.getPassword(), "Circuit");
        } else {
            this.redis = Redis.getInstance(redisCredentials.getHostname(), redisCredentials.getPort(), "Circuit");
        }

        if (mongo == null) {
            this.logger.error("MongoDB connection failed, stopping...");
            System.exit(0);
        }

        if (redis == null) {
            this.logger.error("Redis connection failed, stopping...");
            System.exit(0);
        }

        this.mongoTest();
        this.redisTest();
        gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDAdapter()).registerTypeAdapter(Grant.class, new GrantAdapter()).serializeNulls().create();
    }

    private void redisTest() {
        try {
            CountDownLatch latch = new CountDownLatch(1);

            redis.listen(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (message.equalsIgnoreCase("circuit1")) {
                        redis.sendMessage("circuit2");
                    } else if (message.equalsIgnoreCase("circuit2")) {
                        logger.log("&aCircuit passed the Redis check!");
                        latch.countDown();
                        redis.unlisten(this);
                    }
                }
            }, v -> {
                redis.sendMessage("circuit1");
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                logger.error("Redis test failed: no response within timeout");
                System.exit(0);
            } else {
                RedisPacketRegistry.loadPackets();
                redis.startPacketListener();
                redis.registerListener(new ServerUpdatePacket(), new ServerUpdateListener());
                ServiceContainer.loadClass();
            }
        } catch (Exception e) {
            logger.error("Redis test failed: " + e.getMessage());
            System.exit(0);
        }
    }

    private void mongoTest() {
        try {
            MongoCollection<Document> collection = mongo.getDatabase().getCollection("circuit_test");
            Document testDoc = new Document("_id", UUID.randomUUID().toString()).append("value", "ping");

            collection.insertOne(testDoc);
            Document found = collection.find(new Document("_id", testDoc.getString("_id"))).first();

            if (found != null && found.getString("value").equalsIgnoreCase("ping")) {
                logger.log("&aCircuit passed the MongoDB check!");
                collection.deleteOne(new Document("_id", testDoc.getString("_id")));
                collection.drop();
            } else {
                logger.error("MongoDB test failed: inserted document not found");
                System.exit(0);
            }

        } catch (Exception e) {
            logger.error("MongoDB test failed: " + e.getMessage());
            System.exit(0);
        }
    }

    public void shutdown() {
        if (this.server != null) {
            this.server.setPlayers(0);
            this.server.setOnline(false);
            ServiceContainer.getService(ServerService.class).updateServer(server, true);
        }
        ServiceContainer.shutdownServices();
        this.mongo.close();
        this.redis.close();
    }

    public void setServer(Server server) {
        Validate.notNull(server, "Server cannot be null");
        this.server = server;
        ServiceContainer.getService(ServerService.class).updateServer(server, true);
    }

    public File getFile() {
        try {
            return new File(CircuitShared.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            return new File(".");
        }
    }
}