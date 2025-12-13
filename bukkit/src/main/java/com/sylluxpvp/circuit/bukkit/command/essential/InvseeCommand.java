package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("invsee|inv")
@CommandPermission("circuit.command.invsee")
public class InvseeCommand extends BaseCommand {

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onInvsee(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }
        
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(CC.translate("&cYou cannot view your own inventory with this command."));
            return;
        }

        sender.openInventory(target.getInventory());
    }
}
