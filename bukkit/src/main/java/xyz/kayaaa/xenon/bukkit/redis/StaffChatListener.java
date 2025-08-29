package xyz.kayaaa.xenon.bukkit.redis;

import xyz.kayaaa.xenon.bukkit.tools.spigot.ServerUtils;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.redis.listener.PacketListener;
import xyz.kayaaa.xenon.shared.redis.packets.staff.StaffChatPacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;

public class StaffChatListener extends PacketListener<StaffChatPacket> {
    @Override
    public void listen(StaffChatPacket packet) {
        Profile staff = ServiceContainer.getService(ProfileService.class).find(packet.getStaffUUID());
        ServerUtils.sendMessage( "&b[StaffChat] &f" + staff.getCurrentGrant().getData().getColor() + staff.getName() + " &7[" + packet.getServer() + "]&7: &f" + packet.getMessage(), player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return false;
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }
}
