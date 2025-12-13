package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.chat.ChatChannel;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.packets.staff.AdminChatPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("adminchat|ac")
@CommandPermission("circuit.admin")
public class AdminChatCommand extends BaseCommand {

    private static final String ADMIN_PERMISSION = "circuit.admin";

    @Default
    @Syntax("[message]")
    public void onAdminChat(Player sender, @Optional String message) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());

        if (message == null || message.isEmpty()) {
            // Toggle admin chat mode
            if (profile.getChannel() == ChatChannel.ADMIN) {
                profile.setChannel(ChatChannel.DEFAULT);
                sender.sendMessage(CC.translate("&4[AC] &cDisabled"));
            } else {
                profile.setChannel(ChatChannel.ADMIN);
                sender.sendMessage(CC.translate("&4[AC] &aEnabled"));
            }
            return;
        }

        // Send message directly
        sendAdminMessage(sender, message);
    }

    public static void sendAdminMessage(Player sender, String message) {
        String serverName = CircuitPlugin.getInstance().getShared().getServer().getName();
        Profile profile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());
        String rankColor = profile.getCurrentGrant().getData().getColor();

        String formatted = "&4[AC] &7[" + serverName + "] " + rankColor + sender.getName() + "&7: &f" + message;

        // Notify local admins
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission(ADMIN_PERMISSION)) {
                admin.sendMessage(CC.translate(formatted));
            }
        }

        // Broadcast to other servers via Redis
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new AdminChatPacket(sender.getUniqueId(), serverName, message)
        );
    }
}
