package com.sylluxpvp.circuit.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.TaskUtil;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerUpdatePacket;
import com.sylluxpvp.circuit.shared.server.Server;

public class ServerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TaskUtil.runTaskLater(() -> {
            Server server = CircuitPlugin.getInstance().getShared().getServer();
            server.setWhitelisted(Bukkit.hasWhitelist());
            server.setPlayers(Bukkit.getOnlinePlayers().size());
            server.setMax(Bukkit.getMaxPlayers());
            server.setOnline(true);
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new ServerUpdatePacket(server.getName(), server.getType().name(), server.isOnline(), server.isWhitelisted(), false, server.getPlayers(), server.getMax()));
        }, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TaskUtil.runTaskLater(() -> {
            Server server = CircuitPlugin.getInstance().getShared().getServer();
            server.setWhitelisted(Bukkit.hasWhitelist());
            server.setPlayers(Bukkit.getOnlinePlayers().size());
            server.setMax(Bukkit.getMaxPlayers());
            server.setOnline(true);
            CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new ServerUpdatePacket(server.getName(), server.getType().name(), server.isOnline(), server.isWhitelisted(), false, server.getPlayers(), server.getMax()));
        }, 10L);
    }

}
