package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueLeavePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;

public class QueueLeaveListener extends PacketListener<QueueLeavePacket> {

    @Override
    public void listen(QueueLeavePacket packet) {
        QueueService queueService = ServiceContainer.getService(QueueService.class);
        queueService.removePlayerFromQueue(packet.getPlayerUUID());
    }
}
