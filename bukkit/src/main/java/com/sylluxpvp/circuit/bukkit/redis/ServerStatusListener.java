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
        ServerUtils.sendMessage( "&b[Staff] &f" + packet.getServerName() + " is now " + (packet.isOnline() ? "&aonline" : "&coffline") + (packet.isWhitelisted() ? ", &fbut it is &ewhitelisted." : "."), player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return false;
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }

}
