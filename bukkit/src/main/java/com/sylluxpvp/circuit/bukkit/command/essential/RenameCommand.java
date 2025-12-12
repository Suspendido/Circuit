package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("rename")
@CommandPermission("circuit.command.rename")
public class RenameCommand extends BaseCommand {

    @Default
    @Syntax("<name>")
    public void onRename(Player sender, String name) {
        ItemStack item = sender.getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            sender.sendMessage(CC.translate("&cYou must be holding an item."));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CC.translate(name));
        item.setItemMeta(meta);

        sender.sendMessage(CC.translate("&aItem renamed to: " + name));
    }
}
