package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandAlias("freeze|ss")
@CommandPermission("circuit.command.freeze")
public class FreezeCommand extends BaseCommand {

    private static final Set<UUID> frozenPlayers = new HashSet<>();

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onFreeze(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (sender instanceof Player && !sender.isOp() && target.isOp()) {
            sender.sendMessage(CC.translate("&cYou cannot freeze that player."));
            return;
        }

        if (isFrozen(target.getUniqueId())) {
            unfreeze(target);
            sender.sendMessage(CC.translate("&f" + target.getName() + " &6has been unfrozen."));
        } else {
            freeze(target);
            sender.sendMessage(CC.translate("&f" + target.getName() + " &6has been frozen."));
        }
    }

    @Subcommand("unfreeze")
    @CommandAlias("unfreeze")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onUnfreeze(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (!isFrozen(target.getUniqueId())) {
            sender.sendMessage(CC.translate("&cThat player is not frozen."));
            return;
        }

        unfreeze(target);
        sender.sendMessage(CC.translate("&f" + target.getName() + " &6has been unfrozen."));
    }

    public static void freeze(Player player) {
        frozenPlayers.add(player.getUniqueId());
        player.sendMessage("");
        player.sendMessage(CC.translate("&c&lYou have been frozen by a staff member."));
        player.sendMessage(CC.translate("&cDo not log out or you will be banned."));
        player.sendMessage(CC.translate("&cPlease join our Discord for screenshare."));
        player.sendMessage("");
    }

    public static void unfreeze(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        player.sendMessage(CC.translate("&aYou have been unfrozen."));
    }

    public static boolean isFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    public static Set<UUID> getFrozenPlayers() {
        return frozenPlayers;
    }
}
