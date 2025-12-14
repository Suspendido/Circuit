package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.ReportPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class ReportListener extends PacketListener<ReportPacket> {

    @Override
    public void listen(ReportPacket packet) {
        // Format: [Report][Server] Reporter has reported Target: reason
        ServerUtils.sendMessage("&9[Report] &b[" + packet.getServerName() + "] " + packet.getReporterColor() + packet.getReporterName() + " &7has reported " + packet.getTargetColor() + packet.getTargetName() + "&7: &f" + packet.getReason(), player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null || profile.getCurrentGrant() == null || profile.getCurrentGrant().getData() == null) {
                return player.isOp();
            }
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }
}
