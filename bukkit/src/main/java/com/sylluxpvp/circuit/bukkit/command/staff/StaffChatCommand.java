package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.chat.ChatChannel;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffChatPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("staffchat|sc")
@CommandPermission("circuit.staff")
public class StaffChatCommand extends BaseCommand {

    @Default
    @Syntax("[message]")
    public void onStaffChat(Player sender, @Optional String message) {
        Profile profile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());

        if (message == null || message.isEmpty()) {
            // Toggle staff chat mode
            if (profile.getChannel() == ChatChannel.STAFF) {
                profile.setChannel(ChatChannel.DEFAULT);
                sender.sendMessage(CC.translate("&9[SC] &cDisabled"));
            } else {
                profile.setChannel(ChatChannel.STAFF);
                sender.sendMessage(CC.translate("&9[SC] &aEnabled"));
            }
            return;
        }

        // Send message directly
        sendStaffMessage(sender, message);
    }

    public static void sendStaffMessage(Player sender, String message) {
        String serverName = CircuitPlugin.getInstance().getShared().getServer().getName();
        Profile profile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());
        String rankColor = profile.getCurrentGrant().getData().getColor();

        String formatted = "&9[SC] &b[" + serverName + "] " + rankColor + sender.getName() + "&7: &f" + message;

        // Notify local staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("circuit.staff")) {
                staff.sendMessage(CC.translate(formatted));
            }
        }

        // Broadcast to other servers via Redis
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new StaffChatPacket(sender.getUniqueId(), serverName, formatted)
        );
    }
}
