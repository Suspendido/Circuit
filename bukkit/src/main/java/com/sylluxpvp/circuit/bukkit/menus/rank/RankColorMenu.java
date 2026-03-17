package com.sylluxpvp.circuit.bukkit.menus.rank;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ColorMapping;
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
public class RankColorMenu extends Menu {

    private static final int MENU_SIZE = 45;
    private static final Logger LOGGER = CircuitPlugin.getInstance().getLogger();

    private static final ChatColor[] AVAILABLE_COLORS = {
            ChatColor.BLACK,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_RED,
            ChatColor.DARK_PURPLE,
            ChatColor.GOLD,
            ChatColor.GRAY,
            ChatColor.DARK_GRAY,
            ChatColor.BLUE,
            ChatColor.GREEN,
            ChatColor.AQUA,
            ChatColor.RED,
            ChatColor.LIGHT_PURPLE,
            ChatColor.YELLOW,
            ChatColor.WHITE
    };

    private final Rank rank;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getTitle(Player player) {
        return "&9Select Color for " + rank.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        int slot = 10;
        for (ChatColor color : AVAILABLE_COLORS) {
            while (isBorderSlot(slot)) slot++;
            buttons.put(slot++, new ColorButton(color));
        }

        addBackButton(buttons, new RankEditorMenu(rank));

        return buttons;
    }

    @RequiredArgsConstructor
    private class ColorButton extends Button {

        private final ChatColor chatColor;

        @Override
        public ItemStack getButtonItem(Player player) {
            Material woolMaterial = ColorMapping.getWoolMaterial(chatColor);
            ItemBuilder builder = new ItemBuilder(woolMaterial);
            builder.name(chatColor + formatColorName(chatColor.name()));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Preview: " + chatColor + rank.getName());
            lore.add("");

            String currentColor = rank.getColor();
            if (currentColor != null && currentColor.length() >= 2) {
                ChatColor currentChatColor = ColorMapping.fromString(currentColor);
                if (currentChatColor == chatColor) {
                    lore.add("&a(Selected)");
                } else {
                    lore.add("&aClick to select!");
                }
            } else {
                lore.add("&aClick to select!");
            }

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playSuccess(player);
            rank.setColor("&" + chatColor.getChar());
            ServiceContainer.getService(RankService.class).save(rank);
            player.sendMessage(CC.translate("&aColor updated to: " + chatColor + formatColorName(chatColor.name())));
            LOGGER.info(player.getName() + " changed color of rank " + rank.getName() + " to " + chatColor.name());
            new RankEditorMenu(rank).openMenu(player);
        }

        private String formatColorName(String name) {
            String[] parts = name.toLowerCase().split("_");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                if (!result.isEmpty()) result.append(" ");
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
            return result.toString();
        }
    }
}
