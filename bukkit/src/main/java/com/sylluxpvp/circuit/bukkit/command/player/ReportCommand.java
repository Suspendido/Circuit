package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.redis.packets.staff.StaffChatPacket;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("report")
public class ReportCommand extends BaseCommand {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 60000; // 1 minute

    @Default
    @Syntax("<player> <reason>")
    @CommandCompletion("@players")
    public void onReport(Player sender, String targetName, String reason) {
        if (cooldowns.containsKey(sender.getUniqueId())) {
            long remaining = (cooldowns.get(sender.getUniqueId()) + COOLDOWN_MS) - System.currentTimeMillis();
            if (remaining > 0) {
                sender.sendMessage(CC.translate("&cYou must wait " + (remaining / 1000) + " seconds before reporting again."));
                return;
            }
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (target.equals(sender)) {
            sender.sendMessage(CC.translate("&cYou cannot report yourself."));
            return;
        }

        cooldowns.put(sender.getUniqueId(), System.currentTimeMillis());

        String serverName = CircuitPlugin.getInstance().getShared().getServer().getName();
        String message = "&c[Report] &f" + sender.getName() + " &7reported &f" + target.getName() + " &7for: &f" + reason + " &7(" + serverName + ")";

        // Notify online staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("circuit.staff")) {
                staff.sendMessage(CC.translate(message));
            }
        }

        // Broadcast to other servers via Redis
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new StaffChatPacket(sender.getUniqueId(), serverName, message)
        );

        sender.sendMessage(CC.translate("&aYour report has been submitted. Thank you!"));
    }
}
