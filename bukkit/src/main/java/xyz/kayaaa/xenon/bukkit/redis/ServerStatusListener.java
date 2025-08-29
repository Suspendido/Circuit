package xyz.kayaaa.xenon.bukkit.redis;

import xyz.kayaaa.xenon.bukkit.tools.spigot.ServerUtils;
import xyz.kayaaa.xenon.shared.profile.Profile;
import xyz.kayaaa.xenon.shared.redis.listener.PacketListener;
import xyz.kayaaa.xenon.shared.redis.packets.server.ServerStatusPacket;
import xyz.kayaaa.xenon.shared.service.ServiceContainer;
import xyz.kayaaa.xenon.shared.service.impl.ProfileService;

import java.util.function.Consumer;

public class ServerStatusListener extends PacketListener<ServerStatusPacket> {

    @Override
    public void listen(ServerStatusPacket packet) {
        ServerUtils.sendMessage( "&b[Staff] &f" + packet.getServerName() + " is now " + (packet.isOnline() ? "&aonline" : "&coffline") + (packet.isWhitelisted() ? ", &fbut it is &ewhitelisted." : "."), player -> {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return false;
            return profile.getCurrentGrant().getData().isStaff() || player.isOp();
        });
    }

}
