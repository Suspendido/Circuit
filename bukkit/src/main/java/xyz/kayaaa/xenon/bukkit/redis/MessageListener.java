package xyz.kayaaa.xenon.bukkit.redis;

import xyz.kayaaa.xenon.bukkit.XenonPlugin;
import xyz.kayaaa.xenon.bukkit.tools.spigot.ServerUtils;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.redis.listener.PacketListener;
import xyz.kayaaa.xenon.shared.redis.packets.misc.MessagePacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;

public class MessageListener extends PacketListener<MessagePacket> {

    @Override
    public void listen(MessagePacket packet) {
        String server = packet.getServer();
        if (server == null || server.equalsIgnoreCase(XenonPlugin.getInstance().getShared().getServer().getName())) {
            ServerUtils.sendMessage(packet.getMessage(), player -> {
                Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
                if (profile == null) return false;
                if (!packet.isStaffOnly()) return true;
                return profile.getCurrentGrant().getData().isStaff() || player.isOp();
            });
        }
    }

}
