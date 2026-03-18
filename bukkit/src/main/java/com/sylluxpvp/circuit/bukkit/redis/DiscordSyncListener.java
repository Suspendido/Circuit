package com.sylluxpvp.circuit.bukkit.redis;

import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.discord.DiscordPunishmentPacket;
import com.sylluxpvp.circuit.shared.redis.packets.punish.PunishmentUpdatePacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;

public class DiscordSyncListener extends PacketListener<PunishmentUpdatePacket> {
    @Override
    public void listen(PunishmentUpdatePacket packet) {
        ProfileService profileService = ServiceContainer.getService(ProfileService.class);
        Profile profile = profileService.find(packet.getTarget());
        if (profile == null || profile.getDiscordId() == null) {
            return;
        }
        Profile authorProfile = profileService.find(packet.getAuthor());
        String authorName = authorProfile != null ? authorProfile.getName() : "Console";
        DiscordPunishmentPacket discordPacket = new DiscordPunishmentPacket(
                packet.getTarget(),
                profile.getName(),
                profile.getDiscordId(),
                packet.getPunishmentType(),
                packet.getReason(),
                authorName,
                packet.getDuration(),
                !packet.isRemoved()
        );
        CircuitShared.getInstance().getRedis().sendPacket(discordPacket);
    }
}

