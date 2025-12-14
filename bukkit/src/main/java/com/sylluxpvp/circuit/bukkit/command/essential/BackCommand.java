package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("back")
@CommandPermission("circuit.command.back")
public class BackCommand extends BaseCommand {

    @Default
    public void onBack(Player sender) {
        Location back = TeleportCommand.getBackLocations().get(sender.getUniqueId());

        if (back == null) {
            sender.sendMessage(CC.translate("&cNo previous location recorded."));
            return;
        }

        sender.teleport(back);
        sender.sendMessage(CC.translate("&6Teleporting to your previous location."));
    }
}
