package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("ping|ms|connection")
public class PingCommand extends BaseCommand {

    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onPing(Player sender, @Optional Player target) {
        Player player = target != null ? target : sender;
        int ping = ((CraftPlayer) player).getHandle().ping;

        String color;
        if (ping < 50) color = "&a";
        else if (ping < 100) color = "&e";
        else if (ping < 200) color = "&6";
        else color = "&c";

        if (target != null && !target.equals(sender)) {
            sender.sendMessage(CC.translate("&f" + target.getName() + "&6's Ping: " + color + ping + "ms"));
        } else {
            sender.sendMessage(CC.translate("&6Ping: " + color + ping + "ms"));
        }
    }
}
