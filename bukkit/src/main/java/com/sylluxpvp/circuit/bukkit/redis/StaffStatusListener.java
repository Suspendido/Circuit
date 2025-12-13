package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffStatusPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class StaffStatusListener extends PacketListener<StaffStatusPacket> {
    @Override
    public void listen(StaffStatusPacket packet) {
        // Ignore messages from the same server (already sent locally)
        String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
        if (packet.getServerName() != null && packet.getServerName().equalsIgnoreCase(currentServer)) {
            return;
        }
        
        Profile staff = ServiceContainer.getService(ProfileService.class).find(packet.getStaffUUID());
        String color = packet.isJoined() ? "&a" : "&c";
        String joined = color + (packet.isJoined() ? "joined " : "left ");
        ServerUtils.sendMessage( "&b[Staff] &f" + staff.getCurrentGrant().getData().getColor() + staff.getName() + color + " has " + joined + packet.getServerName() + ".", player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return false;
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }
}
