package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.redis.packets.misc.MessagePacket;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("request|helpop")
public class RequestCommand extends BaseCommand {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 30000; // 30 seconds

    @Default
    @Syntax("<message>")
    public void onRequest(Player sender, String message) {
        if (cooldowns.containsKey(sender.getUniqueId())) {
            long remaining = (cooldowns.get(sender.getUniqueId()) + COOLDOWN_MS) - System.currentTimeMillis();
            if (remaining > 0) {
                sender.sendMessage(CC.translate("&cYou must wait " + (remaining / 1000) + " seconds before requesting again."));
                return;
            }
        }

        cooldowns.put(sender.getUniqueId(), System.currentTimeMillis());

        String serverName = CircuitPlugin.getInstance().getShared().getServer().getName();
        String staffMessage = "&9[Request] &f" + sender.getName() + "&7: &f" + message + " &7(" + serverName + ")";

        // Notify online staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("circuit.staff")) {
                staff.sendMessage(CC.translate(staffMessage));
            }
        }

        // Broadcast to other servers via Redis
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new MessagePacket(serverName, staffMessage, true)
        );

        sender.sendMessage(CC.translate("&aYour request has been sent to staff."));
    }
}
