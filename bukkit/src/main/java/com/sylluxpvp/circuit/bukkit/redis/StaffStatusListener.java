package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffStatusPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class StaffStatusListener extends PacketListener<StaffStatusPacket> {
    @Override
    public void listen(StaffStatusPacket packet) {
        Profile staff = ServiceContainer.getService(ProfileService.class).find(packet.getStaffUUID());
        if (staff == null) return;
        
        String staffColor = staff.getCurrentGrant() != null && staff.getCurrentGrant().getData() != null 
                ? staff.getCurrentGrant().getData().getColor() 
                : "&7";
        String action = packet.isJoined() ? "&ajoined" : "&cleft";
        
        // Format: [Staff] Name has joined/left Server
        ServerUtils.sendMessage("&9[Staff] " + staffColor + staff.getName() + " &7has " + action + " &f" + packet.getServerName() + "&7.", player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null || profile.getCurrentGrant() == null || profile.getCurrentGrant().getData() == null) {
                return player.isOp();
            }
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }
}
