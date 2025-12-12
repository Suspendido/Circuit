package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.service.BukkitChatService;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("slowchat")
@CommandPermission("circuit.command.slowchat")
public class SlowChatCommand extends BaseCommand {

    private static final String STAFF_PERMISSION = "circuit.staff";

    @Default
    @Syntax("[seconds]")
    public void onSlowChat(CommandSender sender, @Default("5") int seconds) {
        BukkitChatService chatService = ServiceContainer.getService(BukkitChatService.class);
        String executor = sender instanceof Player ? ((Player) sender).getName() : "Console";
        long currentSlowdown = chatService.getSlowdown() / 1000;

        if (currentSlowdown == seconds) {
            chatService.setSlowdown(0);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(STAFF_PERMISSION)) {
                    player.sendMessage(CC.translate("&6Chat has been unslowed by &f" + executor + "&6."));
                } else {
                    player.sendMessage(CC.translate("&6Chat has been unslowed."));
                }
            }
            return;
        }

        chatService.setSlowdown(seconds * 1000L);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(STAFF_PERMISSION)) {
                player.sendMessage(CC.translate("&6Chat has been slowed to &f" + seconds + "s &6by &f" + executor + "&6."));
            } else {
                player.sendMessage(CC.translate("&6Chat has been slowed to &f" + seconds + " seconds&6."));
            }
        }
    }
}
