package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.misc.MessagePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class MessageListener extends PacketListener<MessagePacket> {

    @Override
    public void listen(MessagePacket packet) {
        // Ignore messages from the same server (already sent locally)
        String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
        if (packet.getServer() != null && packet.getServer().equalsIgnoreCase(currentServer)) {
            return;
        }
        
        ServerUtils.sendMessage(packet.getMessage(), player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return false;
            if (!packet.isStaffOnly()) return true;
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }

}
