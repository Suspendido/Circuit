package com.sylluxpvp.circuit.bukkit.command.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;
import com.sylluxpvp.circuit.bukkit.menus.PrefixMenu;

@CommandAlias("prefix|prefixes|tags")
public class PrefixCommand extends BaseCommand {

    @Default
    public void prefix(Player sender) {
        new PrefixMenu().openMenu(sender);
    }
}
