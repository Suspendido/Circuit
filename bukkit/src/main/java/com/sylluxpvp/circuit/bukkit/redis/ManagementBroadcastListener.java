package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.broadcast.ManagementBroadcastPacket;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ManagementBroadcastListener extends PacketListener<ManagementBroadcastPacket> {

    @Override
    public void listen(ManagementBroadcastPacket packet) {
        String message = CC.translate(packet.getMessage());
        String permission = packet.getPermission();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
    }
}
