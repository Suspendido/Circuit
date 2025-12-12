package com.sylluxpvp.circuit.shared.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor @Getter @Setter
public class Server {

    private final String name;
    private final ServerType type;

    private boolean online = false;
    private boolean whitelisted = false;
    private int players = 0;
    private int max = 0;

    private String whitelistRank = "default";
    private Set<UUID> whitelistedPlayers = new HashSet<>();

    public String getStatus() {
        return this.online && this.whitelisted ? "&eWhitelisted" : this.online ? "&aOnline" : "&cOffline";
    }

    public boolean isPlayerWhitelisted(UUID uuid) {
        return whitelistedPlayers.contains(uuid);
    }

    public void addWhitelistedPlayer(UUID uuid) {
        whitelistedPlayers.add(uuid);
    }

    public void removeWhitelistedPlayer(UUID uuid) {
        whitelistedPlayers.remove(uuid);
    }

}
