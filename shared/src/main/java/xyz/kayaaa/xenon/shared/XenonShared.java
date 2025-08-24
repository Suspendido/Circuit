package xyz.kayaaa.xenon.shared;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import redis.clients.jedis.JedisPubSub;
import xyz.kayaaa.xenon.shared.mongo.Mongo;
import xyz.kayaaa.xenon.shared.mongo.MongoCredentials;
import xyz.kayaaa.xenon.shared.redis.Redis;
import xyz.kayaaa.xenon.shared.redis.RedisCredentials;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.tools.xenon.XenonLogger;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Getter
public class XenonShared {

    @Getter private static XenonShared instance;
    @Getter @Setter private XenonLogger logger;

    @Getter private final Gson gson = new Gson();
    private final Redis redis;
    private final Mongo mongo;

    public XenonShared(XenonLogger logger, RedisCredentials redisCredentials, MongoCredentials mongoCredentials, String databaseName) {
        instance = this;
        this.logger = logger;
        if (redisCredentials.getPassword() != null && !redisCredentials.getPassword().isEmpty()) {
            this.redis = Redis.getInstance(redisCredentials.getHostname(), redisCredentials.getPort(), redisCredentials.getPassword(), "Xenon");
        } else {
            this.redis = Redis.getInstance(redisCredentials.getHostname(), redisCredentials.getPort(), "Xenon");
        }

        if (mongoCredentials.getUsername() != null && !mongoCredentials.getUsername().isEmpty()) {
            this.mongo = Mongo.getInstance(mongoCredentials.getHostname(), mongoCredentials.getPort(),
                    mongoCredentials.getUsername(), mongoCredentials.getPassword().toCharArray(),
                    mongoCredentials.getAuthSource(), databaseName);
        } else {
            this.mongo = Mongo.getInstance(mongoCredentials.getHostname(), mongoCredentials.getPort(), databaseName);
        }

        this.mongoTest();
        this.redisTest();
    }

    private void redisTest() {
        try {
            logger.log("Redis test started!");
            CountDownLatch latch = new CountDownLatch(1);

            redis.listen(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (message.equalsIgnoreCase("xenon1")) {
                        redis.sendMessage("xenon2");
                    } else if (message.equalsIgnoreCase("xenon2")) {
                        logger.log(true, "Xenon passed the Redis check!");
                        latch.countDown();
                    }
                }
            }, onReady -> {
                redis.sendMessage("xenon1");
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                logger.log(true, "Redis test failed: no response within timeout");
                System.exit(0);
            } else {
                ServiceContainer.loadClass();
            }
        } catch (Exception e) {
            logger.log(true, "Redis test failed: " + e.getMessage());
            System.exit(0);
        }
    }

    private void mongoTest() {
        try {
            logger.log("MongoDB test started!");
            MongoCollection<Document> collection = mongo.getDatabase().getCollection("xenon_test");
            Document testDoc = new Document("_id", UUID.randomUUID().toString()).append("value", "ping");

            collection.insertOne(testDoc);
            Document found = collection.find(new Document("_id", testDoc.getString("_id"))).first();

            if (found != null && found.getString("value").equalsIgnoreCase("ping")) {
                logger.log(true, "Xenon passed the MongoDB check!");
                collection.deleteOne(new Document("_id", testDoc.getString("_id")));
            } else {
                logger.log(true, "MongoDB test failed: inserted document not found");
                System.exit(0);
            }

        } catch (Exception e) {
            logger.log(true, "MongoDB test failed: " + e.getMessage());
            System.exit(0);
        }
    }

    public void shutdown() {
        this.mongo.close();
        this.redis.close();
    }

    public File getFile() {
        try {
            return new File(XenonShared.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            return new File(".");
        }
    }

    private static class XenonDefaultLogger implements XenonLogger {

        @Override
        public void log(boolean prefix, String message) {
            System.out.println((prefix ? "[Xenon - INFO] " : "") + message);
        }

        @Override
        public void log(String message) {
            this.log(true, message);
        }

        @Override
        public void warn(boolean prefix, String message) {
            System.out.println((prefix ? "[Xenon - WARNING] " : "") + message);
        }

        @Override
        public void warn(String message) {
            this.warn(true, message);
        }

        @Override
        public void error(boolean prefix, String message) {
            System.out.println((prefix ? "[Xenon - ERROR] " : "") + message);
        }

        @Override
        public void error(String message) {
            this.error(true, message);
        }
    }
}