package xyz.kayaaa.xenon.shared.redis;

import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import xyz.kayaaa.xenon.shared.XenonShared;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Redis {

    private static Redis instance;
    @Getter
    private final Jedis jedisPublisher;
    private final Jedis jedisSubscriber;
    private final String channel;
    private final Set<Thread> runningListeners = new HashSet<>();

    private Redis(String redisHost, int redisPort, String channel) {
        this(redisHost, redisPort, null, channel);
    }

    private Redis(String redisHost, int redisPort, String password, String channel) {
        this.jedisPublisher = new Jedis(redisHost, redisPort);
        this.jedisSubscriber = new Jedis(redisHost, redisPort);
        this.channel = channel;

        if (password != null && !password.isEmpty()) {
            this.jedisPublisher.auth(password);
            this.jedisSubscriber.auth(password);
        }

        if (!"PONG".equalsIgnoreCase(this.jedisPublisher.ping())) {
            throw new RuntimeException("Failed to connect to Redis!");
        }
    }

    public static synchronized Redis getInstance(String redisHost, int redisPort, String channel) {
        if (instance == null) {
            instance = new Redis(redisHost, redisPort, channel);
        }
        return instance;
    }

    public static synchronized Redis getInstance(String redisHost, int redisPort, String password, String channel) {
        if (instance == null) {
            instance = new Redis(redisHost, redisPort, password, channel);
        }
        return instance;
    }

    public void sendMessage(String message) {
        jedisPublisher.publish(channel, message);
    }

    public synchronized void listen(JedisPubSub listener, Consumer<Void> onReady) {
        Thread thread = new Thread(() -> {
            try {
                jedisSubscriber.subscribe(new JedisPubSub() {
                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        if (onReady != null) onReady.accept(null);
                    }

                    @Override
                    public void onMessage(String channel, String message) {
                        listener.onMessage(channel, message);
                    }
                }, channel);
            } catch (Exception ignored) {}
        });
        thread.setDaemon(true);
        runningListeners.add(thread);
        thread.start();
    }

    public synchronized void listen(JedisPubSub listener) {
        listen(listener, null);
    }

    public void close() {
        try {
            XenonShared.getInstance().getLogger().log(true, "Shutting down Redis connection...");
            jedisPublisher.close();
            jedisSubscriber.close();
        } catch (Exception ignored) {}

        runningListeners.forEach(Thread::interrupt);
        runningListeners.clear();
        instance = null;
    }
}
