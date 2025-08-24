package xyz.kayaaa.xenon.shared;

import com.google.gson.Gson;
import lombok.Getter;
import redis.clients.jedis.JedisPubSub;
import xyz.kayaaa.xenon.shared.mongo.Mongo;
import xyz.kayaaa.xenon.shared.mongo.MongoCredentials;
import xyz.kayaaa.xenon.shared.redis.Redis;
import xyz.kayaaa.xenon.shared.redis.RedisCredentials;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Getter
public class XenonShared {

    @Getter private static XenonShared instance;

    @Getter private final Gson gson = new Gson();
    private final Redis redis;
    private final Mongo mongo;

    public XenonShared(RedisCredentials redisCredentials, MongoCredentials mongoCredentials, String databaseName) {
        instance = this;

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

        this.redisTest();
        this.log("XenonShared was initialized!");
    }

    public XenonShared(RedisCredentials redisCredentials, String databaseName) {
        instance = this;

        if (redisCredentials.getPassword() != null && !redisCredentials.getPassword().isEmpty()) {
            this.redis = Redis.getInstance(redisCredentials.getHostname(), redisCredentials.getPort(), redisCredentials.getPassword(), "Xenon");
        } else {
            this.redis = Redis.getInstance(redisCredentials.getHostname(), redisCredentials.getPort(), "Xenon");
        }

        MongoCredentials defaultMongo = MongoCredentials.getDefault();
        this.mongo = Mongo.getInstance(defaultMongo.getHostname(), defaultMongo.getPort(), databaseName);

        this.redisTest();
        this.log("XenonShared was initialized!");
    }

    public void log(String message) {
        this.log(true, message);
    }

    public void log(boolean prefix, String message) {
        System.out.println((prefix ? "[Xenon] " : "") + message);
    }

    private void redisTest() {
        try {
            this.log("Redis test started!");
            CountDownLatch latch = new CountDownLatch(1);

            redis.listen(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (message.equalsIgnoreCase("xenon1")) {
                        redis.sendMessage("xenon2");
                    } else if (message.equalsIgnoreCase("xenon2")) {
                        log(true, "XenonShared passed the redis check!");
                        latch.countDown();
                    }
                }
            }, onReady -> {
                redis.sendMessage("xenon1");
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                this.log(true, "Redis test failed: no response within timeout");
                System.exit(0);
            } else {
                ServiceContainer.loadClass();
            }
        } catch (Exception e) {
            this.log(true, "Redis test failed: " + e.getMessage());
            System.exit(0);
        }
    }

    public void shutdown() {
        this.redis.close();
        this.mongo.close();
    }

    public File getFile() {
        try {
            return new File(XenonShared.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            return new File(".");
        }
    }
}