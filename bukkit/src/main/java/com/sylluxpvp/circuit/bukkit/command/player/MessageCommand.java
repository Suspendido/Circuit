package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        if (target.equals(sender)) {
            sender.sendMessage(CC.translate("&cYou cannot message yourself."));
            return;
        }

        sendMessage(sender, target, message);
    }

    @CommandAlias("reply|r")
    public void onReply(Player sender, String message) {
        UUID lastUuid = lastMessaged.get(sender.getUniqueId());

        if (lastUuid == null) {
            sender.sendMessage(CC.translate("&cYou have no one to reply to."));
            return;
        }

        Player target = Bukkit.getPlayer(lastUuid);
        if (target == null) {
            sender.sendMessage(CC.translate("&cThat player is no longer online."));
            lastMessaged.remove(sender.getUniqueId());
            return;
        }

        sendMessage(sender, target, message);
    }

    private void sendMessage(Player sender, Player target, String message) {
        sender.sendMessage(CC.translate("&7(To &d" + target.getName() + "&7) &f" + message));
        target.sendMessage(CC.translate("&7(From &d" + sender.getName() + "&7) &f" + message));

        lastMessaged.put(sender.getUniqueId(), target.getUniqueId());
        lastMessaged.put(target.getUniqueId(), sender.getUniqueId());
    }

    public static Map<UUID, UUID> getLastMessaged() {
        return lastMessaged;
    }
}
