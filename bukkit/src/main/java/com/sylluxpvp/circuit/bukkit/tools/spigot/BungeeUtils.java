package com.sylluxpvp.circuit.bukkit.tools.spigot;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import org.bukkit.entity.Player;

public class BungeeUtils {

    public static void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(CircuitPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void registerChannel() {
        CircuitPlugin.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(CircuitPlugin.getInstance(), "BungeeCord");
    }
}
