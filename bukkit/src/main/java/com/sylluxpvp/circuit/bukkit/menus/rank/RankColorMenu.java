package com.sylluxpvp.circuit.bukkit.menus.rank;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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

    private static final ColorEntry[] AVAILABLE_COLORS = {
            new ColorEntry(ChatColor.BLACK, DyeColor.BLACK),
            new ColorEntry(ChatColor.DARK_BLUE, DyeColor.BLUE),
            new ColorEntry(ChatColor.DARK_GREEN, DyeColor.GREEN),
            new ColorEntry(ChatColor.DARK_AQUA, DyeColor.CYAN),
            new ColorEntry(ChatColor.DARK_RED, DyeColor.RED),
            new ColorEntry(ChatColor.DARK_PURPLE, DyeColor.PURPLE),
            new ColorEntry(ChatColor.GOLD, DyeColor.ORANGE),
            new ColorEntry(ChatColor.GRAY, DyeColor.SILVER),
            new ColorEntry(ChatColor.DARK_GRAY, DyeColor.GRAY),
            new ColorEntry(ChatColor.BLUE, DyeColor.LIGHT_BLUE),
            new ColorEntry(ChatColor.GREEN, DyeColor.LIME),
            new ColorEntry(ChatColor.AQUA, DyeColor.LIGHT_BLUE),
            new ColorEntry(ChatColor.RED, DyeColor.RED),
            new ColorEntry(ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA),
            new ColorEntry(ChatColor.YELLOW, DyeColor.YELLOW),
            new ColorEntry(ChatColor.WHITE, DyeColor.WHITE)
    };

    @RequiredArgsConstructor
    private static class ColorEntry {
        private final ChatColor chatColor;
        private final DyeColor dyeColor;
    }

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
        for (ColorEntry entry : AVAILABLE_COLORS) {
            while (isBorderSlot(slot)) slot++;
            buttons.put(slot++, new ColorButton(entry));
        }

        addBackButton(buttons, new RankEditorMenu(rank));

        return buttons;
    }

    @RequiredArgsConstructor
    private class ColorButton extends Button {

        private final ColorEntry entry;

        @Override
        @SuppressWarnings("deprecation")
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.WOOL);
            builder.durability(entry.dyeColor.getWoolData());
            builder.name(entry.chatColor + formatColorName(entry.chatColor.name()));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Preview: " + entry.chatColor + rank.getName());
            lore.add("");

            String currentColor = rank.getColor();
            if (currentColor != null && currentColor.length() >= 2) {
                ChatColor currentChatColor = ColorMapping.fromString(currentColor);
                if (currentChatColor == entry.chatColor) {
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
            rank.setColor("&" + entry.chatColor.getChar());
            ServiceContainer.getService(RankService.class).save(rank);
            player.sendMessage(CC.translate("&aColor updated to: " + entry.chatColor + formatColorName(entry.chatColor.name())));
            LOGGER.info(player.getName() + " changed color of rank " + rank.getName() + " to " + entry.chatColor.name());
            new RankEditorMenu(rank).openMenu(player);
        }

        private String formatColorName(String name) {
            String[] parts = name.toLowerCase().split("_");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
            return result.toString();
        }
    }
}
