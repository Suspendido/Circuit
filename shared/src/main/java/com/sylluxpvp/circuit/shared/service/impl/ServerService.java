package com.sylluxpvp.circuit.shared.service.impl;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import com.sylluxpvp.circuit.shared.CircuitShared;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerStatusPacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerUpdatePacket;
import com.sylluxpvp.circuit.shared.server.Server;
import com.sylluxpvp.circuit.shared.server.ServerType;
import com.sylluxpvp.circuit.shared.service.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class ServerService extends Service {

    private List<Server> servers;

    @Override
    public @NonNull String getIdentifier() {
        return "server";
    }

    @Override
    public void enable() {
        this.servers = new ArrayList<>();
    }

    @Override
    public void disable() {
        if (this.servers != null) {
            this.servers.clear();
            this.servers = null;
        }
    }

    public Optional<Server> find(String name) {
        Validate.notNull(name, "Name cannot be null");
        return servers.stream()
                .filter(server -> server.getName().equals(name))
                .findFirst();
    }

    public Optional<Server> find(String name, ServerType type) {
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(type, "Type cannot be null");
        return servers.stream()
                .filter(server -> server.getName().equals(name) && server.getType().equals(type))
                .findFirst();
    }

    public Optional<Server> find(ServerType type) {
        Validate.notNull(type, "Type cannot be null");
        return servers.stream()
                .filter(server -> server.getType().equals(type))
                .findFirst();
    }

    public List<Server> findAll(ServerType type) {
        Validate.notNull(type, "Type cannot be null");
        return servers.stream()
                .filter(server -> server.getType().equals(type))
                .collect(Collectors.toList());
    }

    public void addServer(Server server) {
        Validate.notNull(server, "Server cannot be null");
        if (!servers.contains(server)) {
            servers.add(server);
        }
    }

    public void updateServer(ServerUpdatePacket packet) {
        Validate.notNull(packet, "Packet cannot be null");

        Server server = find(packet.getServerName()).orElseGet(() -> {
            Server newServer = new Server(packet.getServerName(),
                    ServerType.valueOf(packet.getServerType().toUpperCase()));
            addServer(newServer);
            return newServer;
        });

        updateServerData(server, packet.isOnline(), packet.getPlayers(), packet.getMax(), packet.isWhitelisted(), packet.isSendStatus());
    }

    public void updateServer(Server server, boolean sendStatus) {
        Validate.notNull(server, "Server cannot be null");

        Server cached = find(server.getName()).orElseGet(() -> {
            addServer(server);
            return server;
        });

        updateServerData(cached, server.isOnline(), server.getPlayers(), server.getMax(), server.isWhitelisted(), sendStatus);
    }

    private void updateServerData(Server server, boolean online, int players, int max, boolean whitelisted, boolean sendStatus) {
        server.setOnline(online);
        server.setPlayers(players);
        server.setMax(max);
        server.setWhitelisted(whitelisted);
        if (sendStatus) sendServerStatusPacket(server);
        logServerUpdate(server);
    }

    private void sendServerStatusPacket(Server server) {
        ServerStatusPacket packet = new ServerStatusPacket(server.getName(), server.isOnline(), server.isWhitelisted());
        CircuitShared.getInstance().getRedis().sendPacket(packet);
    }

    private void logServerUpdate(Server server) {
        CircuitShared.getInstance().getLogger()
                .log(String.format("Updated server %s (Online: %s, Whitelisted: %s, Players: %d/%d)",
                        server.getName(), server.isOnline(), server.isWhitelisted(), server.getPlayers(), server.getMax()));
    }

    public boolean removeServer(String name) {
        Validate.notNull(name, "Name cannot be null");
        return servers.removeIf(server -> server.getName().equals(name));
    }

    public boolean removeServers(ServerType type) {
        Validate.notNull(type, "Type cannot be null");
        return servers.removeIf(server -> server.getType().equals(type));
    }

    public int getServerCount() {
        return servers.size();
    }

    public int getOnlineServerCount() {
        return (int) servers.stream().filter(Server::isOnline).count();
    }

    public int getTotalPlayers() {
        return servers.stream()
                .filter(Server::isOnline)
                .mapToInt(Server::getPlayers)
                .sum();
    }
}