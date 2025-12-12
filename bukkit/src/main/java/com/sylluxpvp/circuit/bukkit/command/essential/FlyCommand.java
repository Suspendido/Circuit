package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("fly")
@CommandPermission("circuit.command.fly")
public class FlyCommand extends BaseCommand {

    @Default
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onFly(Player sender, @Optional Player target) {
        Player player = target != null ? target : sender;

        if (target != null && !sender.hasPermission("circuit.command.fly.others")) {
            sender.sendMessage(CC.translate("&cYou don't have permission to toggle fly for others."));
            return;
        }

        player.setAllowFlight(!player.getAllowFlight());
        player.setFlying(player.getAllowFlight());

        String status = player.getAllowFlight() ? "&aEnabled" : "&cDisabled";
        player.sendMessage(CC.translate("&6Fly: " + status));

        if (target != null && !target.equals(sender)) {
            sender.sendMessage(CC.translate("&6Set fly to " + status + " &6for &f" + target.getName() + "&6."));
        }
    }
}
