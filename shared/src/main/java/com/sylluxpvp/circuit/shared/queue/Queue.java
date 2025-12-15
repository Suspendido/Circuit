package com.sylluxpvp.circuit.shared.queue;

import lombok.Getter;
import lombok.Setter;

import java.util.PriorityQueue;
import java.util.UUID;

@Getter
public class Queue {

    private final String serverName;
    @Setter
    private PriorityQueue<QueuePlayer> players = new PriorityQueue<>();
    @Setter
    private boolean enabled = true;
    @Setter
    private boolean paused = false;
    
    // Stats for time estimation
    private long lastSendTime = System.currentTimeMillis();
    private int recentSendCount = 0;
    private static final int ESTIMATED_SECONDS_PER_PLAYER = 3; // Default fallback

    public Queue(String serverName) {
        this.serverName = serverName;
    }
    
    public void recordSend() {
        this.recentSendCount++;
        this.lastSendTime = System.currentTimeMillis();
    }
    
    public int getEstimatedWaitSeconds(int position) {
        // Estimate based on position * seconds per player
        return position * ESTIMATED_SECONDS_PER_PLAYER;
    }

    public boolean containsPlayer(UUID uuid) {
        return players.stream().anyMatch(p -> p.getUuid().equals(uuid));
    }

    public QueuePlayer getPlayer(UUID uuid) {
        return players.stream().filter(p -> p.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public boolean removePlayer(UUID uuid) {
        return players.removeIf(p -> p.getUuid().equals(uuid));
    }

    public void addPlayer(QueuePlayer player) {
        if (!containsPlayer(player.getUuid())) {
            players.add(player);
        }
    }

    public int getPosition(UUID uuid) {
        if (!containsPlayer(uuid)) {
            return -1;
        }

        PriorityQueue<QueuePlayer> copy = new PriorityQueue<>(players);
        int position = 1;

        while (!copy.isEmpty()) {
            QueuePlayer player = copy.poll();
            if (player.getUuid().equals(uuid)) {
                return position;
            }
            position++;
        }

        return -1;
    }

    public QueuePlayer poll() {
        return players.poll();
    }

    public QueuePlayer peek() {
        return players.peek();
    }

    public int size() {
        return players.size();
    }
}
