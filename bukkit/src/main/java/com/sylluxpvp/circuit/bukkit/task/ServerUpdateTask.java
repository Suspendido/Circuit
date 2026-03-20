package com.sylluxpvp.circuit.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.TaskUtil;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerUpdatePacket;
import com.sylluxpvp.circuit.shared.server.Server;

public class ServerUpdateTask extends BukkitRunnable {

    public ServerUpdateTask() {
        TaskUtil.runTaskTimerAsynchronously(this, 20 * 30, 20 * 30); // Heartbeat every 30 seconds
    }

    @Override
    public void run() {
        Server server = CircuitPlugin.getInstance().getShared().getServer();
        server.setWhitelisted(Bukkit.hasWhitelist());
        server.setPlayers(Bukkit.getOnlinePlayers().size());
        server.setMax(Bukkit.getMaxPlayers());
        server.setOnline(true);
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(new ServerUpdatePacket(server.getName(), server.getType().name(),
                server.isOnline(), server.isWhitelisted(), false, server.getPlayers(), server.getMax(),
                server.getWhitelistRank(), server.getWhitelistedPlayers()));
    }
}
