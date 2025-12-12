package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("feed")
@CommandPermission("circuit.command.feed")
public class FeedCommand extends BaseCommand {

    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onFeed(Player sender, @Optional Player target) {
        Player player = target != null ? target : sender;

        if (target != null && !sender.hasPermission("circuit.command.feed.others")) {
            sender.sendMessage(CC.translate("&cYou don't have permission to feed others."));
            return;
        }

        player.setFoodLevel(20);
        player.setSaturation(20);
        player.sendMessage(CC.translate("&aYou have been fed."));

        if (target != null && !target.equals(sender)) {
            sender.sendMessage(CC.translate("&6Fed &f" + target.getName() + "&6."));
        }
    }
}
