package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.UUID;

@CommandAlias("reply|r")
public class ReplyCommand extends BaseCommand {

    @Default
    @Syntax("<message>")
    public void onReply(Player sender, String message) {
        UUID lastUuid = MessageCommand.getLastMessaged().get(sender.getUniqueId());

        if (lastUuid == null) {
            sender.sendMessage(CC.translate("&cYou have no one to reply to."));
            return;
        }

        Player target = Bukkit.getPlayer(lastUuid);
        if (target == null) {
            sender.sendMessage(CC.translate("&cThat player is no longer online."));
            MessageCommand.getLastMessaged().remove(sender.getUniqueId());
            return;
        }

        MessageCommand.sendMessage(sender, target, message);
    }
}
