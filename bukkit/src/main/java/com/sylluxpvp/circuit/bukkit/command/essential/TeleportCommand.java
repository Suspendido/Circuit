package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.tools.spigot.StaffBroadcast;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandAlias("teleport|tp|goto")
@CommandPermission("circuit.command.teleport")
public class TeleportCommand extends BaseCommand {

    private static final Map<UUID, Location> backLocations = new HashMap<>();

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onTeleport(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }
        
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(CC.translate("&cYou cannot teleport to yourself."));
            return;
        }

        backLocations.put(sender.getUniqueId(), sender.getLocation());
        sender.teleport(target);
        sender.sendMessage(CC.translate("&6Teleporting to &f" + target.getName() + "&6."));
        
        StaffBroadcast.broadcastLocal("&f" + StaffBroadcast.getDisplayName(sender) + " &7has teleported to &f" + StaffBroadcast.getDisplayName(target) + "&7.", "circuit.admin");
    }

    public static Map<UUID, Location> getBackLocations() {
        return backLocations;
    }

    public static void saveBackLocation(Player player) {
        backLocations.put(player.getUniqueId(), player.getLocation());
    }
}
