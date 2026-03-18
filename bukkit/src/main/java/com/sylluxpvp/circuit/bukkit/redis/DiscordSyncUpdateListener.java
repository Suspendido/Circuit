package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.discord.DiscordSyncUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class DiscordSyncUpdateListener extends PacketListener<DiscordSyncUpdatePacket> {
    @Override
    public void listen(DiscordSyncUpdatePacket packet) {
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.find(packet.getPlayerUUID());
        if (profile != null) {
            String discordIdStr = packet.getDiscordId();
            Long discordId = discordIdStr != null && !discordIdStr.equals("null") ? Long.valueOf(Long.parseLong(discordIdStr)) : null;
            profile.setDiscordId(discordId);
            System.out.println("[Circuit] Updated discordId for " + profile.getName() + " to " + discordId);
        }
    }
}

