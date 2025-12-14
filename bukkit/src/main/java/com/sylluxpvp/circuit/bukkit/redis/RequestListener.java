package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.RequestPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class RequestListener extends PacketListener<RequestPacket> {

    @Override
    public void listen(RequestPacket packet) {
        // Format: [Request][Server] Name has requested assistance: message
        ServerUtils.sendMessage("&9[Request] &b[" + packet.getServerName() + "] " + packet.getPlayerColor() + packet.getPlayerName() + " &7has requested assistance: &f" + packet.getMessage(), player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null || profile.getCurrentGrant() == null || profile.getCurrentGrant().getData() == null) {
                return player.isOp();
            }
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }
}
