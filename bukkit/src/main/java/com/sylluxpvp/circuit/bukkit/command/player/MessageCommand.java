package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("msg|message|tell|whisper|w|pm")
public class MessageCommand extends BaseCommand {

    private static final Map<UUID, UUID> lastMessaged = new HashMap<>();

    @Default
    @Syntax("<player> <message>")
    @CommandCompletion("@players")
    public void onMessage(Player sender, String targetName, String message) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (target.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendMessage(CC.translate("&cYou cannot message yourself."));
            return;
        }

        sendMessage(sender, target, message);
    }

    public static void sendMessage(Player sender, Player target, String message) {
        Profile senderProfile = ServiceContainer.getService(ProfileService.class).find(sender.getUniqueId());
        Profile targetProfile = ServiceContainer.getService(ProfileService.class).find(target.getUniqueId());

        String senderColor = "&f";
        if (senderProfile != null && senderProfile.getCurrentGrant() != null && senderProfile.getCurrentGrant().getData() != null) {
            senderColor = senderProfile.getCurrentGrant().getData().getColor();
        }

        String targetColor = "&f";
        if (targetProfile != null && targetProfile.getCurrentGrant() != null && targetProfile.getCurrentGrant().getData() != null) {
            targetColor = targetProfile.getCurrentGrant().getData().getColor();
        }

        sender.sendMessage(CC.translate("&7(To " + targetColor + target.getName() + "&7) &f" + message));
        target.sendMessage(CC.translate("&7(From " + senderColor + sender.getName() + "&7) &f" + message));

        lastMessaged.put(sender.getUniqueId(), target.getUniqueId());
        lastMessaged.put(target.getUniqueId(), sender.getUniqueId());
    }

    public static Map<UUID, UUID> getLastMessaged() {
        return lastMessaged;
    }
}
