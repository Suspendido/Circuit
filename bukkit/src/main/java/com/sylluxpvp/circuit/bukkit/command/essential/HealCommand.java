package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("heal")
@CommandPermission("circuit.command.heal")
public class HealCommand extends BaseCommand {

    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onHeal(Player sender, @Optional Player target) {
        Player player = target != null ? target : sender;

        if (target != null && !sender.hasPermission("circuit.command.heal.others")) {
            sender.sendMessage(CC.translate("&cYou don't have permission to heal others."));
            return;
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.sendMessage(CC.translate("&aYou have been healed."));

        if (target != null && !target.equals(sender)) {
            sender.sendMessage(CC.translate("&6Healed &f" + target.getName() + "&6."));
        }
    }
}
