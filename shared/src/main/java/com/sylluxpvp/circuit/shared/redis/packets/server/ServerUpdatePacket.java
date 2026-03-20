package com.sylluxpvp.circuit.shared.redis.packets.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sylluxpvp.circuit.shared.redis.RedisPacket;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ServerUpdatePacket implements RedisPacket {

    private String serverName;
    private String serverType;
    private boolean online;
    private boolean whitelisted;
    private boolean sendStatus;
    private int players;
    private int max;
    private String whitelistRank;
    private Set<UUID> whitelistedPlayers;

    @Override
    public String getID() {
        return "SERVER_UPDATE";
    }
}