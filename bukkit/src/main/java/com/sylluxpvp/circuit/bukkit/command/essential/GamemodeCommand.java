package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.tools.spigot.StaffBroadcast;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("gamemode|gm")
@CommandPermission("circuit.command.gamemode")
public class GamemodeCommand extends BaseCommand {

    @Default
    @Syntax("<mode> [player]")
    @CommandCompletion("@gamemodes @players")
    public void onGamemode(Player sender, GameMode mode, @Optional Player target) {
        Player player = target != null ? target : sender;

        if (target != null && !sender.hasPermission("circuit.command.gamemode.others")) {
            sender.sendMessage(CC.translate("&cYou don't have permission to change others' gamemode."));
            return;
        }

        player.setGameMode(mode);
        player.sendMessage(CC.translate("&6Gamemode: &f" + mode.name()));

        if (target != null && !target.equals(sender)) {
            sender.sendMessage(CC.translate("&6Set gamemode to &f" + mode.name() + " &6for &f" + target.getName() + "&6."));
            StaffBroadcast.broadcastLocal("&f" + StaffBroadcast.getDisplayName(sender) + " &7set &f" + StaffBroadcast.getDisplayName(target) + "&7's gamemode to &f" + mode.name() + "&7.", "circuit.admin");
        } else {
            StaffBroadcast.broadcastLocal("&f" + StaffBroadcast.getDisplayName(sender) + " &7set their gamemode to &f" + mode.name() + "&7.", "circuit.admin");
        }
    }

    @CommandAlias("gmc|gm1")
    @CommandPermission("circuit.command.gamemode")
    public void onCreative(Player sender, @Optional Player target) {
        onGamemode(sender, GameMode.CREATIVE, target);
    }

    @CommandAlias("gms|gm0")
    @CommandPermission("circuit.command.gamemode")
    public void onSurvival(Player sender, @Optional Player target) {
        onGamemode(sender, GameMode.SURVIVAL, target);
    }

    @CommandAlias("gma|gm2")
    @CommandPermission("circuit.command.gamemode")
    public void onAdventure(Player sender, @Optional Player target) {
        onGamemode(sender, GameMode.ADVENTURE, target);
    }

    @CommandAlias("gmsp|gm3")
    @CommandPermission("circuit.command.gamemode")
    public void onSpectator(Player sender, @Optional Player target) {
        onGamemode(sender, GameMode.SPECTATOR, target);
    }
}
