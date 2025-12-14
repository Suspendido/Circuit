package com.sylluxpvp.circuit.bukkit.command.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("unfreeze")
@CommandPermission("circuit.command.freeze")
public class UnfreezeCommand extends BaseCommand {

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onUnfreeze(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."));
            return;
        }

        if (!FreezeCommand.isFrozen(target.getUniqueId())) {
            sender.sendMessage(CC.translate("&cThat player is not frozen."));
            return;
        }

        FreezeCommand.unfreeze(target);
        sender.sendMessage(CC.translate("&f" + target.getName() + " &6has been unfrozen."));
    }
}
