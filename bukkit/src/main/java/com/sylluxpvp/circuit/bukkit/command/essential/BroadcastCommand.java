package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("broadcast|bc|raw")
@CommandPermission("circuit.command.broadcast")
public class BroadcastCommand extends BaseCommand {

    @Default
    @Syntax("<message>")
    public void onBroadcast(CommandSender sender, String message) {
        String translated = CC.translate(message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(translated);
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(translated);
        }
    }
}
