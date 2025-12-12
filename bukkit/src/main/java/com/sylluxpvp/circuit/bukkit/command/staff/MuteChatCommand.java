package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.service.BukkitChatService;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("mutechat|mc")
@CommandPermission("circuit.command.mutechat")
public class MuteChatCommand extends BaseCommand {

    private static final String STAFF_PERMISSION = "circuit.staff";

    @Default
    public void onMuteChat(CommandSender sender) {
        BukkitChatService chatService = ServiceContainer.getService(BukkitChatService.class);
        chatService.toggleChat();
        boolean enabled = chatService.isChatEnabled();

        String executor = sender instanceof Player ? ((Player) sender).getName() : "Console";
        String status = enabled ? "&aunmuted" : "&cmuted";

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(STAFF_PERMISSION)) {
                player.sendMessage(CC.translate("&dChat has been " + status + " &dby &f" + executor + "&d."));
            } else {
                player.sendMessage(CC.translate("&dChat has been " + status + "&d."));
            }
        }
    }
}
