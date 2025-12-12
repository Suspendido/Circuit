package com.sylluxpvp.circuit.bukkit.command.grant;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.menus.GrantsMenu;

@CommandAlias("grants")
@CommandPermission("circuit.grant.view")
public class GrantsCommand extends BaseCommand {

    @Default
    @Description("View all grants of a player")
    @CommandCompletion("@players")
    public void grants(Player sender, @Optional @Name("target") OfflinePlayer target) {
        if (target == null) {
            new GrantsMenu(sender.getUniqueId()).openMenu(sender);
            return;
        }

        new GrantsMenu(target.getUniqueId()).openMenu(sender);
    }
}
