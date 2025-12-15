package com.sylluxpvp.circuit.shared.service.impl;

import com.sylluxpvp.circuit.shared.queue.Queue;
import com.sylluxpvp.circuit.shared.queue.QueuePlayer;
import com.sylluxpvp.circuit.shared.service.Service;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class QueueService extends Service {

    private final Map<String, Queue> queues = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 3000; // 3 seconds

    @Override
    public @NonNull String getIdentifier() {
        return "queue";
    }

    @Override
    public void enable() {
        // Queues are created dynamically when servers come online
    }

    @Override
    public void disable() {
        queues.clear();
        cooldowns.clear();
    }
    
    public boolean isOnCooldown(UUID uuid) {
        Long lastAction = cooldowns.get(uuid);
        if (lastAction == null) return false;
        return System.currentTimeMillis() - lastAction < COOLDOWN_MS;
    }
    
    public long getRemainingCooldown(UUID uuid) {
        Long lastAction = cooldowns.get(uuid);
        if (lastAction == null) return 0;
        long remaining = COOLDOWN_MS - (System.currentTimeMillis() - lastAction);
        return Math.max(0, remaining);
    }
    
    public void setCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public Queue getQueue(String serverName) {
        return queues.get(serverName.toLowerCase());
    }

    public Queue getOrCreateQueue(String serverName) {
        String key = serverName.toLowerCase();
        return queues.computeIfAbsent(key, k -> new Queue(serverName));
    }

    public void removeQueue(String serverName) {
        queues.remove(serverName.toLowerCase());
    }

    public Optional<Queue> findQueueByPlayer(UUID uuid) {
        return queues.values().stream()
                .filter(q -> q.containsPlayer(uuid))
                .findFirst();
    }

    public void addPlayerToQueue(String serverName, QueuePlayer player) {
        Queue queue = getOrCreateQueue(serverName);
        queue.addPlayer(player);
    }

    public boolean removePlayerFromQueue(UUID uuid) {
        Optional<Queue> queue = findQueueByPlayer(uuid);
        if (queue.isPresent()) {
            return queue.get().removePlayer(uuid);
        }
        return false;
    }

    public int getPlayerPosition(UUID uuid) {
        Optional<Queue> queue = findQueueByPlayer(uuid);
        return queue.map(q -> q.getPosition(uuid)).orElse(-1);
    }

    public String getPlayerQueueName(UUID uuid) {
        Optional<Queue> queue = findQueueByPlayer(uuid);
        return queue.map(Queue::getServerName).orElse(null);
    }
}
