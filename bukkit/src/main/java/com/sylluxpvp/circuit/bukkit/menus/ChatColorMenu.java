package com.sylluxpvp.circuit.bukkit.menus;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ColorMapping;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.profile.Profile;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.ProfileService;
import com.sylluxpvp.circuit.shared.tools.string.CC;
import com.sylluxpvp.circuit.shared.tools.string.StringHelper;

import java.util.*;

public class ChatColorMenu extends Menu {

    private static final int MENU_SIZE = 45;

    @Override
    public String getTitle(Player player) {
        return "&9Chat Color";
    }

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
        buttons.put(4, new CurrentColorButton(profile));

        int slot = 10;
        for (ChatColor color : getAvailableColors()) {
            while (isBorderSlot(slot) && slot < MENU_SIZE - 9) slot++;
            if (slot >= MENU_SIZE - 9) break;
            buttons.put(slot++, new ChatColorButton(color));
        }

        buttons.put(MENU_SIZE - 6, new ResetColorButton());
        buttons.put(MENU_SIZE - 5, new CloseButton());

        return buttons;
    }

    private List<ChatColor> getAvailableColors() {
        List<ChatColor> colors = new ArrayList<>();
        colors.add(ChatColor.DARK_RED);
        colors.add(ChatColor.RED);
        colors.add(ChatColor.GOLD);
        colors.add(ChatColor.YELLOW);
        colors.add(ChatColor.DARK_GREEN);
        colors.add(ChatColor.GREEN);
        colors.add(ChatColor.AQUA);
        colors.add(ChatColor.DARK_AQUA);
        colors.add(ChatColor.DARK_BLUE);
        colors.add(ChatColor.BLUE);
        colors.add(ChatColor.LIGHT_PURPLE);
        colors.add(ChatColor.DARK_PURPLE);
        colors.add(ChatColor.WHITE);
        colors.add(ChatColor.GRAY);
        colors.add(ChatColor.DARK_GRAY);
        colors.add(ChatColor.BLACK);
        return colors;
    }

    private class CurrentColorButton extends Button {

        private final Profile profile;

        public CurrentColorButton(Profile profile) {
            this.profile = profile;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM);
            builder.skull(player.getName());

            String currentColor = profile != null && !profile.getColor().isEmpty()
                    ? profile.getColor()
                    : "&7";
            ChatColor chatColor = ColorMapping.fromString(currentColor);
            String colorName = chatColor != null
                    ? StringHelper.capitalizeAllWords(chatColor.name().toLowerCase().replace("_", " "))
                    : "None";

            builder.name(currentColor + player.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&9&lCurrent Color");
            lore.add("&fColor: " + currentColor + colorName);
            lore.add("");
            lore.add("&7Preview:");
            if (profile != null) {
                lore.add(profile.getCurrentGrant().getData().getPrefix() + profile.getCurrentGrant().getData().getColor() + player.getName() + "&7: " + currentColor + "Hello!");
            }

            builder.lore(lore);
            return builder.build();
        }
    }

    @RequiredArgsConstructor
    private class ChatColorButton extends Button {

        private final ChatColor color;

        @SuppressWarnings("deprecation")
        @Override
        public ItemStack getButtonItem(Player player) {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            boolean isSelected = profile != null && ColorMapping.isChatColor(profile.getColor(), color);

            DyeColor dyeColor = ColorMapping.chatColorToDyeColor(color);
            ItemBuilder builder = new ItemBuilder(Material.WOOL);
            builder.durability(dyeColor != null ? dyeColor.getWoolData() : 0);

            String colorName = StringHelper.capitalizeAllWords(color.name().toLowerCase().replace("_", " "));
            builder.name(color + colorName + (isSelected ? " &a✓" : ""));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Preview:");
            if (profile != null) {
                lore.add(profile.getCurrentGrant().getData().getPrefix() + profile.getCurrentGrant().getData().getColor() + player.getName() + "&7: " + color + "Hello!");
            }
            lore.add("");
            lore.add(isSelected ? "&aCurrently selected!" : "&aClick to select!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return;

            if (ColorMapping.isChatColor(profile.getColor(), color)) {
                player.sendMessage(CC.translate("&cYou're already using this color."));
                return;
            }

            profile.setColor("&" + color.getChar());
            ServiceContainer.getService(ProfileService.class).save(profile);
            Button.playSuccess(player);

            String colorName = StringHelper.capitalizeAllWords(color.name().toLowerCase().replace("_", " "));
            player.sendMessage(CC.translate("&aYour chat color is now " + color + colorName + "&a!"));

            new ChatColorMenu().openMenu(player);
        }
    }

    private class ResetColorButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.INK_SACK);
            builder.durability(15);
            builder.name("&c&lReset Color");

            List<String> lore = new ArrayList<>();
            lore.add("&7Remove your custom chat color.");
            lore.add("");
            lore.add("&cClick to reset!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = ServiceContainer.getService(ProfileService.class).find(player.getUniqueId());
            if (profile == null) return;

            if (profile.getColor().isEmpty()) {
                player.sendMessage(CC.translate("&cYou don't have a custom color."));
                return;
            }

            profile.setColor("");
            ServiceContainer.getService(ProfileService.class).save(profile);
            Button.playNeutral(player);

            player.sendMessage(CC.translate("&aYour chat color has been reset!"));
            new ChatColorMenu().openMenu(player);
        }
    }

    private class CloseButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.REDSTONE);
            builder.name("&c&lClose");

            List<String> lore = new ArrayList<>();
            lore.add("&7Close this menu.");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
        }
    }
}
