package com.sylluxpvp.circuit.shared.redis.packets.discord;

import com.sylluxpvp.circuit.shared.redis.RedisPacket;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DiscordGrantUpdatePacket implements RedisPacket {
    private final UUID playerUUID;
    private final String playerName;
    private final String discordId;
    private final String rankName;
    private final String rankColor;
    private final boolean added;

    public DiscordGrantUpdatePacket(UUID playerUUID, String playerName, String discordId, String rankName, String rankColor, boolean added) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.discordId = discordId;
        this.rankName = rankName;
        this.rankColor = rankColor;
        this.added = added;
    }

    @Override
    public String getID() {
        return "DISCORD_GRANT_UPDATE";
    }

}

