package com.sylluxpvp.circuit.bukkit.redis;

import org.bukkit.Bukkit;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerDiscoveryPacket;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerUpdatePacket;
import com.sylluxpvp.circuit.shared.server.Server;

public class ServerDiscoveryListener extends PacketListener<ServerDiscoveryPacket> {

    @Override
    public void listen(ServerDiscoveryPacket packet) {
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        if (server == null) return;
        
        // Don't respond to our own discovery request
        if (server.getName().equalsIgnoreCase(packet.getRequestingServer())) {
            return;
        }
        
        // Respond with our server info
        server.setWhitelisted(Bukkit.hasWhitelist());
        server.setPlayers(Bukkit.getOnlinePlayers().size());
        server.setMax(Bukkit.getMaxPlayers());
        
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new ServerUpdatePacket(server.getName(), server.getType().name(), 
                        server.isOnline(), server.isWhitelisted(), false, 
                        server.getPlayers(), server.getMax())
        );
    }
}
