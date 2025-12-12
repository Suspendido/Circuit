package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
    public void onTeleport(Player sender, Player target) {
        if (sender.equals(target)) {
            sender.sendMessage(CC.translate("&cYou cannot teleport to yourself."));
            return;
        }

        backLocations.put(sender.getUniqueId(), sender.getLocation());
        sender.teleport(target);
        sender.sendMessage(CC.translate("&6Teleporting to &f" + target.getName() + "&6."));
    }

    @CommandAlias("tphere|s|bring")
    @CommandPermission("circuit.command.teleport.here")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onTeleportHere(Player sender, Player target) {
        if (sender.equals(target)) {
            sender.sendMessage(CC.translate("&cYou cannot teleport yourself to yourself."));
            return;
        }

        backLocations.put(target.getUniqueId(), target.getLocation());
        target.teleport(sender);
        sender.sendMessage(CC.translate("&6Teleporting &f" + target.getName() + " &6to you."));
        target.sendMessage(CC.translate("&6You have been teleported to &f" + sender.getName() + "&6."));
    }

    @CommandAlias("tppos")
    @CommandPermission("circuit.command.teleport.pos")
    @Syntax("<x> <y> <z>")
    public void onTeleportPos(Player sender, double x, double y, double z) {
        backLocations.put(sender.getUniqueId(), sender.getLocation());

        if (x % 1.0 == 0.0) x += 0.5;
        if (z % 1.0 == 0.0) z += 0.5;

        Location location = new Location(sender.getWorld(), x, y, z);
        sender.teleport(location);
        sender.sendMessage(CC.translate("&6Teleporting to &e[&f" + x + "&e, &f" + y + "&e, &f" + z + "&e]&6."));
    }

    @CommandAlias("back")
    @CommandPermission("circuit.command.back")
    public void onBack(Player sender) {
        Location back = backLocations.get(sender.getUniqueId());

        if (back == null) {
            sender.sendMessage(CC.translate("&cNo previous location recorded."));
            return;
        }

        sender.teleport(back);
        sender.sendMessage(CC.translate("&6Teleporting to your previous location."));
    }

    public static void saveBackLocation(Player player) {
        backLocations.put(player.getUniqueId(), player.getLocation());
    }
}
