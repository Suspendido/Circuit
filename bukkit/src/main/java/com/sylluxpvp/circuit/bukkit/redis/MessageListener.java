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
        String server = packet.getServer();
        if (server == null || server.equalsIgnoreCase(CircuitPlugin.getInstance().getShared().getServer().getName())) {
            ServerUtils.sendMessage(packet.getMessage(), player -> {
                Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
                if (profile == null) return false;
                if (!packet.isStaffOnly()) return true;
                return profile.getCurrentGrant().getData().isStaff() || player.isOp();
            });
        }
    }

}
