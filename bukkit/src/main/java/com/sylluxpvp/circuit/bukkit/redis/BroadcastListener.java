package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.broadcast.BroadcastPacket;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastListener extends PacketListener<BroadcastPacket> {

    @Override
    public void listen(BroadcastPacket packet) {
        String message = CC.translate(packet.getMessage());
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
