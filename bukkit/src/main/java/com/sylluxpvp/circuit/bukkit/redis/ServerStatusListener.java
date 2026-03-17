package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerStatusPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class ServerStatusListener extends PacketListener<ServerStatusPacket> {

    @Override
    public void listen(ServerStatusPacket packet) {
        String status = packet.isOnline() ? "&aonline" : "&coffline";
        String suffix = packet.isOnline() ? " &7and may be joined." : " &7and may no longer be joined.";

        if (packet.isWhitelisted()) {
            suffix = "&7, but it is &ewhitelisted&7.";
        }
        
        // Format: [Server] ServerName is now online/offline and may be joined.
        ServerUtils.sendMessage("&9[Server] &f" + packet.getServerName() + " &7is now " + status + suffix, player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return false;
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }

}
