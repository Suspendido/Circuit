package com.sylluxpvp.circuit.bukkit.task;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.queue.Queue;
import com.sylluxpvp.circuit.shared.queue.QueuePlayer;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueuePositionPacket;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueSendPacket;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import com.sylluxpvp.circuit.shared.service.impl.ServerService;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.PriorityQueue;

public class QueueTask extends BukkitRunnable {

    private static final long SEND_DELAY_TICKS = 5L; // 0.25 seconds
    private static final long POSITION_UPDATE_INTERVAL = 60L; // 3 seconds
    private static final int MAX_SENDS_PER_CYCLE = 3; // Max players to send per cycle per queue
    
    private long tickCount = 0;

    public QueueTask() {
        this.runTaskTimerAsynchronously(CircuitPlugin.getInstance(), 10L, SEND_DELAY_TICKS);
    }

    @Override
    public void run() {
        tickCount++;
        
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        ServerService serverService = ServiceContainer.getService(ServerService.class);
        
        for (Queue queue : queueService.getQueues().values()) {
            if (!queue.isEnabled() || queue.isPaused() || queue.size() == 0) {
                continue;
            }
            
            Optional<Server> serverOpt = serverService.find(queue.getServerName());
            if (!serverOpt.isPresent()) {
                continue;
            }
            
            Server server = serverOpt.get();
            if (!server.isOnline() || server.isWhitelisted()) {
                continue;
            }
            
            int availableSlots = server.getMax() - server.getPlayers();
            if (availableSlots <= 0) {
                continue;
            }
            
            // Send multiple players per cycle (up to available slots or max per cycle)
            int toSend = Math.min(Math.min(queue.size(), availableSlots), MAX_SENDS_PER_CYCLE);
            for (int i = 0; i < toSend; i++) {
                QueuePlayer nextPlayer = queue.poll();
                if (nextPlayer != null) {
                    CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                            new QueueSendPacket(nextPlayer.getUuid(), queue.getServerName())
                    );
                }
            }
        }
        
        // Send position updates every 3 seconds
        if (tickCount % (POSITION_UPDATE_INTERVAL / SEND_DELAY_TICKS) == 0) {
            sendPositionUpdates(queueService);
        }
    }
    
    private void sendPositionUpdates(QueueService queueService) {
        for (Queue queue : queueService.getQueues().values()) {
            if (queue.size() == 0) {
                continue;
            }
            
            PriorityQueue<QueuePlayer> copy = new PriorityQueue<>(queue.getPlayers());
            int position = 1;
            int total = queue.size();
            
            while (!copy.isEmpty()) {
                QueuePlayer player = copy.poll();
                
                // Only send if player is online on this server
                if (Bukkit.getPlayer(player.getUuid()) != null) {
                    int estimatedSeconds = queue.getEstimatedWaitSeconds(position);
                    CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                            new QueuePositionPacket(player.getUuid(), queue.getServerName(), position, total, estimatedSeconds)
                    );
                }
                position++;
            }
        }
    }
}
