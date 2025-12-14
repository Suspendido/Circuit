package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.tools.spigot.StaffBroadcast;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("tppos")
@CommandPermission("circuit.command.teleport.pos")
public class TeleportPosCommand extends BaseCommand {

    @Default
    @Syntax("<x> <y> <z>")
    public void onTeleportPos(Player sender, double x, double y, double z) {
        TeleportCommand.getBackLocations().put(sender.getUniqueId(), sender.getLocation());

        if (x % 1.0 == 0.0) x += 0.5;
        if (z % 1.0 == 0.0) z += 0.5;

        Location location = new Location(sender.getWorld(), x, y, z);
        sender.teleport(location);
        sender.sendMessage(CC.translate("&6Teleporting to &e[&f" + x + "&e, &f" + y + "&e, &f" + z + "&e]&6."));
        
        StaffBroadcast.broadcastLocal("&f" + StaffBroadcast.getDisplayName(sender) + " &7has teleported to &f" + (int)x + ", " + (int)y + ", " + (int)z + "&7.", "circuit.admin");
    }
}
