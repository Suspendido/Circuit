package com.sylluxpvp.circuit.shared.redis;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Redis {

    private static Redis instance;

    private final String host;
    private final int port;
    private final String password;

    @Getter
    private final Jedis jedisPublisher;
    private final Jedis jedisSubscriber;
    private final String channel;
    private final Map<JedisPubSub, Thread> runningListeners = new HashMap<>();
    private final Map<String, List<PacketListener<RedisPacket>>> packetListeners = new ConcurrentHashMap<>();

    private Redis(String redisHost, int redisPort, String channel) {
        this(redisHost, redisPort, null, channel);
    }

    private Redis(String redisHost, int redisPort, String password, String channel) {
        this.host = redisHost;
        this.port = redisPort;
        this.password = password;
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
        return getInstance(redisHost, redisPort, null, channel);
    }

    public static synchronized Redis getInstance(String redisHost, int redisPort, String password, String channel) {
        try {
            if (instance == null) {
                instance = new Redis(redisHost, redisPort, password, channel);
            }
            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    public void sendMessage(String message) {
        jedisPublisher.publish(channel, message);
    }

    public synchronized void listen(JedisPubSub listener, Consumer<Void> onReady) {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = new Jedis(host, port)) {
                if (password != null) jedis.auth(password);
                jedis.subscribe(new JedisPubSub() {
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
        runningListeners.put(listener, thread);
        thread.start();
    }

    public synchronized void unlisten(JedisPubSub listener) {
        Validate.notNull(listener, "Listener cannot be null");
        try {
            Thread thread = runningListeners.remove(listener);
            if (thread != null) thread.interrupt();
            listener.unsubscribe(channel);
        } catch (Exception ignored) {}
    }

    public synchronized void listen(JedisPubSub listener) {
        listen(listener, null);
    }

    public void close() {
        try {
            jedisPublisher.close();
            jedisSubscriber.close();
        } catch (Exception ignored) {}

        runningListeners.values().forEach(Thread::interrupt);
        runningListeners.clear();
        instance = null;
    }

    public void sendPacket(RedisPacket packet) {
        String json = CircuitShared.getInstance().getGson().toJson(packet);
        sendMessage(packet.getID() + "|" + json);
    }

    public void startPacketListener() {
        listen(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                String[] parts = message.split("\\|", 2);
                if (parts.length == 2) {
                    String packetId = parts[0];
                    String payload = parts[1];

                    Class<? extends RedisPacket> packetClass = RedisPacketRegistry.get(packetId);
                    if (packetClass != null) {
                        RedisPacket packet = CircuitShared.getInstance().getGson().fromJson(payload, packetClass);
                        handlePacket(packet);
                    } else {
                        CircuitShared.getInstance().getLogger().warn("Unknown packet ID: " + packetId);
                    }
                }
            }
        }, v -> {});
    }

    public <T extends RedisPacket> void registerListener(T packet, PacketListener<T> listener) {
        packetListeners.computeIfAbsent(packet.getID(), id -> new ArrayList<>()).add((PacketListener<RedisPacket>) listener);
    }

    public void handlePacket(RedisPacket packet) {
        List<PacketListener<RedisPacket>> packetListeners = this.packetListeners.get(packet.getID());
        if (packetListeners == null || packetListeners.isEmpty()) {
            CircuitShared.getInstance().getLogger().warn("No listeners for " + packet.getID() + " were registered, ignoring...");
            return;
        }

        packetListeners.forEach(l -> {
            try {
                l.accept(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
