package xyz.kayaaa.xenon.bukkit.command.impl;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import xyz.kayaaa.xenon.bukkit.command.CommandBase;
import xyz.kayaaa.xenon.bukkit.menus.GrantsMenu;

public class GrantsCommand extends CommandBase {

    public GrantsCommand() {
        super("grants", false);
    }

    @Command(name = "", desc = "View all of a player's grants", usage = "<target>")
    @Require("xenon.grant.view")
    public void grants(@Sender Player player, @OptArg OfflinePlayer target) {
        if (target == null) {
            new GrantsMenu(player.getUniqueId()).openMenu(player);
            return;
        }

        new GrantsMenu(target.getUniqueId()).openMenu(player);
    }

}
