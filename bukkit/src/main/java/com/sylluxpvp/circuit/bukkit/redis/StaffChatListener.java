package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffChatPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

public class StaffChatListener extends PacketListener<StaffChatPacket> {

    @Override
    public void listen(StaffChatPacket packet) {

        Profile staff = ServiceContainer
                .getService(ProfileService.class)
                .find(packet.getStaffUUID());

        if (staff == null) {
            return;
        }

        String message =
                "&b[SC] "
                        + "&7[" + packet.getServer() + "] "
                        + staff.getCurrentGrant().getData().getColor()
                        + staff.getName()
                        + "&7: &f"
                        + CC.removeColors(CC.translate(packet.getMessage()));

        ServerUtils.sendMessage(message, player -> {
            Profile profile = ServiceContainer
                    .getService(ProfileService.class)
                    .find(player.getUniqueId());

            if (profile == null) return false;

            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }
}
