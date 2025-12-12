package com.sylluxpvp.circuit.bukkit.redis;

import org.bukkit.Bukkit;
import org.bukkit.permissions.ServerOperator;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ServerUtils;
import com.sylluxpvp.circuit.bukkit.tools.spigot.TaskUtil;
import com.sylluxpvp.circuit.shared.redis.listener.PacketListener;
import com.sylluxpvp.circuit.shared.redis.packets.server.ServerCommandPacket;

public class ServerCommandListener extends PacketListener<ServerCommandPacket> {

    @Override
    public void listen(ServerCommandPacket packet) {
        String server = packet.getServer();
        if (server == null || server.equalsIgnoreCase(CircuitPlugin.getInstance().getShared().getServer().getName())) {
            ServerUtils.sendMessage( "&b[Circuit] &fA command packet has been sent" + (server == null ? " globally " : " ") + "with command \"/" + packet.getCommand() + "\", executing in some moments!", ServerOperator::isOp);
            TaskUtil.runTaskLater(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), packet.getCommand()), 60L);
        }
    }

}
