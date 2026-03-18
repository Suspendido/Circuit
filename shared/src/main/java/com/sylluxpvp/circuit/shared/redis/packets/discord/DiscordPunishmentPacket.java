package com.sylluxpvp.circuit.shared.redis.packets.discord;

import com.sylluxpvp.circuit.shared.redis.RedisPacket;
import java.util.UUID;
import lombok.Generated;
import lombok.Getter;

@Getter
public class DiscordPunishmentPacket implements RedisPacket {
    private final UUID playerUUID;
    private final String playerName;
    private final Long discordId;
    private final String punishmentType;
    private final String reason;
    private final String punisherName;
    private final long duration;
    private final boolean active;

    public DiscordPunishmentPacket(UUID playerUUID, String playerName, Long discordId, String punishmentType, String reason, String punisherName, long duration, boolean active) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.discordId = discordId;
        this.punishmentType = punishmentType;
        this.reason = reason;
        this.punisherName = punisherName;
        this.duration = duration;
        this.active = active;
    }

    @Override
    public String getID() {
        return "DISCORD_PUNISHMENT";
    }

}

