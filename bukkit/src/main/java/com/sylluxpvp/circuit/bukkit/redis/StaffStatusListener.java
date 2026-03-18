package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffStatusPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import org.jetbrains.annotations.Nullable;

public class StaffStatusListener extends PacketListener<StaffStatusPacket> {

    @Override
    public void listen(StaffStatusPacket packet) {
        Profile staff = ServiceContainer.getService(ProfileService.class).find(packet.getStaffUUID());
        if (staff == null) return;

        String message = getMessage(packet, staff);

        if (message == null) return;

        String finalMessage = message;
        ServerUtils.sendMessage(finalMessage, player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null || profile.getCurrentGrant() == null || profile.getCurrentGrant().getData() == null) {
                return player.isOp();
            }
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }

    @Nullable
    private static String getMessage(StaffStatusPacket packet, Profile staff) {
        String staffColor = staff.getCurrentGrant() != null && staff.getCurrentGrant().getData() != null ? staff.getCurrentGrant().getData().getColor() : "&7";
        String message = switch (packet.getAction()) {
            case "networkConnect" -> "&9[Staff] " + staffColor + staff.getName() + "&a entró &fal servidor &7(&6" + packet.getServerName() + "&7)";
            case "networkDisconnect" -> "&9[Staff] " + staffColor + staff.getName() + " &fse&c desconectó &fdel servidor &7(&fDesde &6" + packet.getFromServerName() + "&7)";
            case "serverSwitch" -> "&9[Staff] " + staffColor + staff.getName() + " &fcambio del server &6" + packet.getFromServerName() + " &fa &6" + packet.getServerName();
            default -> null;
        };
        return message;
    }
}
