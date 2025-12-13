package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.AdminChatPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class AdminChatListener extends PacketListener<AdminChatPacket> {

    private static final String ADMIN_PERMISSION = "circuit.admin";

    @Override
    public void listen(AdminChatPacket packet) {
        // Ignore messages from the same server (already sent locally)
        String currentServer = CircuitPlugin.getInstance().getShared().getServer().getName();
        if (packet.getServer().equals(currentServer)) {
            return;
        }
        
        Profile admin = ServiceContainer
                .getService(ProfileService.class)
                .find(packet.getAdminUUID());

        if (admin == null) {
            return;
        }

        String message =
                "&4[AC] "
                        + "&7[" + packet.getServer() + "] "
                        + admin.getCurrentGrant().getData().getColor()
                        + admin.getName()
                        + "&7: &f"
                        + packet.getMessage();

        ServerUtils.sendMessage(message, player -> player.hasPermission(ADMIN_PERMISSION));
    }
}
