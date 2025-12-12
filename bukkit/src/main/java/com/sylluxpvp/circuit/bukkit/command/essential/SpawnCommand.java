package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("spawn")
public class SpawnCommand extends BaseCommand {

    @Default
    public void onSpawn(Player sender) {
        Location spawn = getSpawnLocation();

        if (spawn == null) {
            sender.sendMessage(CC.translate("&cSpawn has not been set."));
            return;
        }

        sender.teleport(spawn);
        sender.sendMessage(CC.translate("&aTeleported to spawn."));
    }

    @CommandAlias("setspawn")
    @CommandPermission("circuit.command.setspawn")
    public void onSetSpawn(Player sender) {
        Location loc = sender.getLocation();
        CircuitPlugin plugin = CircuitPlugin.getInstance();

        plugin.getMainConfig().set("spawn.world", loc.getWorld().getName());
        plugin.getMainConfig().set("spawn.x", loc.getX());
        plugin.getMainConfig().set("spawn.y", loc.getY());
        plugin.getMainConfig().set("spawn.z", loc.getZ());
        plugin.getMainConfig().set("spawn.yaw", loc.getYaw());
        plugin.getMainConfig().set("spawn.pitch", loc.getPitch());

        sender.sendMessage(CC.translate("&aSpawn has been set to your location."));
    }

    public static Location getSpawnLocation() {
        CircuitPlugin plugin = CircuitPlugin.getInstance();

        if (!plugin.getMainConfig().contains("spawn.world")) {
            return null;
        }

        String worldName = plugin.getMainConfig().getString("spawn.world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return null;
        }

        double x = plugin.getMainConfig().getDouble("spawn.x");
        double y = plugin.getMainConfig().getDouble("spawn.y");
        double z = plugin.getMainConfig().getDouble("spawn.z");
        float yaw = (float) plugin.getMainConfig().getDouble("spawn.yaw");
        float pitch = (float) plugin.getMainConfig().getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }
}
