package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("clearchat|cc")
@CommandPermission("circuit.command.clearchat")
public class ClearChatCommand extends BaseCommand {

    private static final String STAFF_PERMISSION = "circuit.staff";

    @Default
    public void onClearChat(CommandSender sender) {
        String executor = sender instanceof Player ? ((Player) sender).getName() : "Console";

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(STAFF_PERMISSION)) {
                player.sendMessage(CC.translate("&7[&5C&7] &f" + executor + " &7has &dcleared &7the chat."));
                continue;
            }

            for (int i = 0; i < 100; i++) {
                player.sendMessage("");
            }
        }
    }
}
