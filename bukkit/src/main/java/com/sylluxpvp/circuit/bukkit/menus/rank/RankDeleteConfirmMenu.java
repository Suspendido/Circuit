package com.sylluxpvp.circuit.bukkit.menus.rank;

import lombok.RequiredArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.rank.Rank;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.RankService;
import com.sylluxpvp.circuit.shared.tools.string.CC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RankDeleteConfirmMenu extends Menu {

    private static final int MENU_SIZE = 27;
    private static final int CONFIRM_SLOT = 11;
    private static final int CANCEL_SLOT = 15;

    private final Rank rank;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getTitle(Player player) {
        return "&cDelete " + rank.getColor() + rank.getName() + "&c?";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        buttons.put(CONFIRM_SLOT, new ConfirmButton());
        buttons.put(CANCEL_SLOT, new CancelButton());

        return buttons;
    }

    @SuppressWarnings("deprecation")
    private class ConfirmButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.LIME_WOOL);
            builder.name("&a&lConfirm Delete");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7This will permanently delete");
            lore.add("&7the rank " + rank.getColor() + rank.getName() + "&7.");
            lore.add("");
            lore.add("&aClick to confirm!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playSuccess(player);
            RankService service = ServiceContainer.getService(RankService.class);
            String rankName = rank.getName();
            String rankColor = rank.getColor();
            service.delete(rank);
            player.sendMessage(CC.translate("&aRank " + rankColor + rankName + " &ahas been deleted!"));
            CircuitPlugin.getInstance().getLogger().info(player.getName() + " deleted rank: " + rankName);
            new RankListMenu().openMenu(player);
        }
    }

    @SuppressWarnings("deprecation")
    private static class CancelButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.RED_WOOL);
            builder.name("&c&lCancel");

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Go back to rank list.");
            lore.add("");
            lore.add("&cClick to cancel!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new RankListMenu().openMenu(player);
        }
    }
}
