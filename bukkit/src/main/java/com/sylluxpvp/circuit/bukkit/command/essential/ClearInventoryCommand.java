package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("clear|ci|clearinventory")
@CommandPermission("circuit.command.clear")
public class ClearInventoryCommand extends BaseCommand {

    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onClear(Player sender, @Optional Player target) {
        Player player = target != null ? target : sender;

        if (target != null && !sender.hasPermission("circuit.command.clear.others")) {
            sender.sendMessage(CC.translate("&cYou don't have permission to clear others' inventory."));
            return;
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        String whose = target != null && !target.equals(sender) 
                ? target.getName() + "'s" 
                : "your";
        sender.sendMessage(CC.translate("&6Cleared " + whose + " inventory."));
    }
}
