package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.tools.spigot.BungeeUtils;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueueSendPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.QueueService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QueueSendListener extends PacketListener<QueueSendPacket> {

    @Override
    public void listen(QueueSendPacket packet) {
        Player player = Bukkit.getPlayer(packet.getPlayerUUID());
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Remove from queue
        ServiceContainer.getService(QueueService.class).removePlayerFromQueue(packet.getPlayerUUID());
        
        // Send to server via BungeeCord
        BungeeUtils.sendToServer(player, packet.getTargetServer());
    }
}
