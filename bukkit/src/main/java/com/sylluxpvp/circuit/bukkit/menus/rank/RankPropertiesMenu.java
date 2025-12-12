package com.sylluxpvp.circuit.bukkit.menus.rank;

import lombok.RequiredArgsConstructor;
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
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RankPropertiesMenu extends Menu {

    private static final int MENU_SIZE = 45;
    private static final Logger LOGGER = CircuitPlugin.getInstance().getLogger();

    private final Rank rank;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getTitle(Player player) {
        return "&9Properties: " + rank.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        buttons.put(10, new ToggleButton("Staff", rank.isStaff(), () -> {
            rank.setStaff(!rank.isStaff());
            return rank.isStaff();
        }));

        buttons.put(12, new ToggleButton("Default", rank.isDefaultRank(), () -> {
            if (!rank.isDefaultRank()) {
                RankService service = ServiceContainer.getService(RankService.class);
                Rank currentDefault = service.getDefaultRank();
                if (currentDefault != null && currentDefault != rank) {
                    return null;
                }
            }
            rank.setDefaultRank(!rank.isDefaultRank());
            return rank.isDefaultRank();
        }));

        buttons.put(14, new ToggleButton("Hidden", rank.isHidden(), () -> {
            rank.setHidden(!rank.isHidden());
            return rank.isHidden();
        }));

        buttons.put(16, new ToggleButton("Purchasable", rank.isPurchasable(), () -> {
            rank.setPurchasable(!rank.isPurchasable());
            return rank.isPurchasable();
        }));

        buttons.put(MENU_SIZE - 5, new BackToEditorButton());

        return buttons;
    }

    private class BackToEditorButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.REDSTONE);
            builder.name("&c&lBack to Editor");

            List<String> lore = new ArrayList<>();
            lore.add("&7Return to the rank editor.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new RankEditorMenu(rank).openMenu(player);
        }
    }

    @FunctionalInterface
    private interface ToggleAction {
        Boolean toggle();
    }

    @RequiredArgsConstructor
    private class ToggleButton extends Button {

        private final String propertyName;
        private final boolean currentValue;
        private final ToggleAction toggleAction;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(currentValue ? Material.INK_SACK : Material.INK_SACK);
            builder.durability(currentValue ? 10 : 8);
            builder.name((currentValue ? "&a" : "&c") + propertyName);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Current: " + (currentValue ? "&aEnabled" : "&cDisabled"));
            lore.add("");
            lore.add("&eClick to toggle!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Boolean result = toggleAction.toggle();

            if (result == null) {
                Button.playFail(player);
                player.sendMessage(CC.translate("&cAnother rank is already set as default! Disable it first."));
                return;
            }

            Button.playSuccess(player);
            ServiceContainer.getService(RankService.class).save(rank);
            player.sendMessage(CC.translate("&a" + propertyName + " set to: " + (result ? "&aEnabled" : "&cDisabled")));
            LOGGER.info(player.getName() + " toggled " + propertyName + " of rank " + rank.getName() + " to " + result);
            new RankPropertiesMenu(rank).openMenu(player);
        }
    }
}
