package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("invsee|inv")
@CommandPermission("circuit.command.invsee")
public class InvseeCommand extends BaseCommand {

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onInvsee(Player sender, Player target) {
        if (sender.equals(target)) {
            sender.sendMessage(CC.translate("&cYou cannot view your own inventory with this command."));
            return;
        }

        sender.openInventory(target.getInventory());
    }
}
