package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.tools.spigot.StaffBroadcast;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("tphere|s|bring")
@CommandPermission("circuit.command.teleport.here")
public class TeleportHereCommand extends BaseCommand {

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onTeleportHere(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }
        
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(CC.translate("&cYou cannot teleport yourself to yourself."));
            return;
        }

        TeleportCommand.getBackLocations().put(target.getUniqueId(), target.getLocation());
        target.teleport(sender);
        sender.sendMessage(CC.translate("&6Teleporting &f" + target.getName() + " &6to you."));
        target.sendMessage(CC.translate("&6You have been teleported to &f" + sender.getName() + "&6."));
        
        StaffBroadcast.broadcastLocal("&f" + StaffBroadcast.getDisplayName(sender) + " &7has teleported &f" + StaffBroadcast.getDisplayName(target) + " &7to them.", "circuit.admin");
    }
}
