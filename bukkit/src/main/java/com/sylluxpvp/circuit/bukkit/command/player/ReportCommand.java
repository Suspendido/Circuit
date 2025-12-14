package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.redis.packets.staff.ReportPacket;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("report")
public class ReportCommand extends BaseCommand {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 60_000L;

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

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(CC.translate("&cYou cannot report yourself."));
            return;
        }

        cooldowns.put(sender.getUniqueId(), System.currentTimeMillis());

        Profile reporterProfile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());
        String reporterColor = "&7";
        if (reporterProfile != null && reporterProfile.getCurrentGrant() != null && reporterProfile.getCurrentGrant().getData() != null) {
            reporterColor = reporterProfile.getCurrentGrant().getData().getColor();
        }

        Profile targetProfile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());
        String targetColor = "&7";
        if (targetProfile != null && targetProfile.getCurrentGrant() != null && targetProfile.getCurrentGrant().getData() != null) {
            targetColor = targetProfile.getCurrentGrant().getData().getColor();
        }

        String serverName = CircuitPlugin.getInstance().getShared().getServer().getName();

        // Send via Redis to all servers
        CircuitPlugin.getInstance().getShared().getRedis().sendPacket(
                new ReportPacket(sender.getUniqueId(), sender.getName(), reporterColor, target.getName(), targetColor, serverName, reason)
        );

        sender.sendMessage(CC.translate("&aYour report has been submitted. Thank you!"));
    }
}
