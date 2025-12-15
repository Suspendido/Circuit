package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffChatPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class StaffChatListener extends PacketListener<StaffChatPacket> {

    @Override
    public void listen(StaffChatPacket packet) {
        // Ignore messages from the same server (already sent locally)
        String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
        if (packet.getServer() != null && packet.getServer().equalsIgnoreCase(currentServer)) {
            return;
        }

        Profile staff = ServiceContainer
                .getService(ProfileService.class)
                .find(packet.getStaffUUID());

        if (staff == null) {
            return;
        }

        String message =
                "&9[SC] "
                        + "&b[" + packet.getServer() + "] "
                        + staff.getCurrentGrant().getData().getColor()
                        + staff.getName()
                        + "&7: &f"
                        + packet.getMessage();

        ServerUtils.sendMessageNoConsole(message, player -> {
            Profile profile = ServiceContainer
                    .getService(ProfileService.class)
                    .find(player.getUniqueId());

            if (profile == null) return false;

            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }
}
