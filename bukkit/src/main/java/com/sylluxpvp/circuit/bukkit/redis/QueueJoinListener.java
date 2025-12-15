package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.shared.queue.Queue;
import com.sylluxpvp.circuit.shared.queue.QueuePlayer;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueJoinPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;

public class QueueJoinListener extends PacketListener<QueueJoinPacket> {

    @Override
    public void listen(QueueJoinPacket packet) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        Queue queue = queueService.getOrCreateQueue(packet.getTargetServer());
        
        // Remove from any existing queue first
        queueService.removePlayerFromQueue(packet.getPlayerUUID());
        
        // Add to the new queue
        QueuePlayer queuePlayer = new QueuePlayer(
                packet.getPlayerUUID(),
                packet.getPlayerName(),
                packet.getPriority(),
                System.currentTimeMillis()
        );
        queue.addPlayer(queuePlayer);
    }
}
