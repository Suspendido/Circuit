package com.sylluxpvp.circuit.shared.redis.packets.discord;

import com.sylluxpvp.circuit.shared.redis.RedisPacket;
import java.util.UUID;
import lombok.Generated;
import lombok.Getter;

@Getter
public class DiscordSyncUpdatePacket implements RedisPacket {
    private UUID playerUUID;
    private String discordId;

    @Override
    public String getID() {
        return "DISCORD_SYNC_UPDATE";
    }
}

