package com.sylluxpvp.circuit.bukkit.command.essential;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.shared.tools.string.CC;

@CommandAlias("more")
@CommandPermission("circuit.command.more")
public class MoreCommand extends BaseCommand {

    @Default
    public void onMore(Player sender) {
        ItemStack item = sender.getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            sender.sendMessage(CC.translate("&cYou must be holding an item."));
            return;
        }

        item.setAmount(64);
        sender.sendMessage(CC.translate("&aYour item stack has been filled to 64."));
    }
}
