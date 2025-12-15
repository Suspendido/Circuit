package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.queue.QueuePositionPacket;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QueuePositionListener extends PacketListener<QueuePositionPacket> {

    @Override
    public void listen(QueuePositionPacket packet) {
        Player player = Bukkit.getPlayer(packet.getPlayerUUID());
        if (player == null || !player.isOnline()) {
            return;
        }
        
        String timeStr = formatTime(packet.getEstimatedSeconds());
        player.sendMessage(CC.translate("&9[Queue] &7Position: &f#" + packet.getPosition() + "&7/&f" + packet.getTotal() + 
                " &7for &f" + packet.getQueueName() + " &7(~" + timeStr + ")"));
    }
    
    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return minutes + "m " + secs + "s";
    }
}
