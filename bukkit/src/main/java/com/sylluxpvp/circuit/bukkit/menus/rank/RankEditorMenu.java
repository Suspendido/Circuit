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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class RankEditorMenu extends Menu {

    private static final int MENU_SIZE = 45;
    private static final Logger LOGGER = CircuitPlugin.getInstance().getLogger();

    private final Rank rank;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getTitle(Player player) {
        return "&9Editing: " + rank.getColor() + rank.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        buttons.put(10, new DisplayInfoButton());
        buttons.put(12, new EditPrefixButton());
        buttons.put(14, new EditSuffixButton());
        buttons.put(16, new EditColorButton());

        buttons.put(20, new EditWeightButton());
        buttons.put(22, new PermissionsButton());
        buttons.put(24, new InheritancesButton());

        buttons.put(30, new TogglePropertiesButton());
        buttons.put(32, new DiscordLinkButton());

        addBackButton(buttons, new RankListMenu());

        return buttons;
    }

    private class DisplayInfoButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.INK_SACK);
            builder.durability(ColorMapping.getItemDurability(rank.getColor()));
            builder.name(rank.getColor() + rank.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&9&lRank Information");
            lore.add("&fPrefix: " + (rank.getPrefix() == null || rank.getPrefix().isEmpty() ? "&cNone" : rank.getPrefix() + player.getName()));
            lore.add("&fSuffix: " + (rank.getSuffix() == null || rank.getSuffix().isEmpty() ? "&cNone" : player.getName() + rank.getSuffix()));
            lore.add("&fWeight: &9" + rank.getWeight());
            lore.add("&fColor: " + rank.getColor() + "Sample");
            lore.add("");
            lore.add("&9&lRank Properties");
            lore.add("&fStaff: " + (rank.isStaff() ? "&aYes" : "&cNo"));
            lore.add("&fDefault: " + (rank.isDefaultRank() ? "&aYes" : "&cNo"));
            lore.add("&fHidden: " + (rank.isHidden() ? "&aYes" : "&cNo"));
            lore.add("&fPurchasable: " + (rank.isPurchasable() ? "&aYes" : "&cNo"));
            lore.add("");
            lore.add("&fPermissions: &9" + rank.getPermissions().size());
            lore.add("&fInheritances: &9" + rank.getInheritances().size());

            builder.lore(lore);
            return builder.build();
        }
    }

    private class EditPrefixButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.NAME_TAG);
            builder.name("&9&lEdit Prefix");

            List<String> lore = new ArrayList<>();
            lore.add("&7Current: " + (rank.getPrefix() == null || rank.getPrefix().isEmpty() ? "&cNone" : rank.getPrefix() + "Player"));
            lore.add("");
            lore.add("&aClick to edit!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new RankInputHandler(player, rank, RankInputHandler.InputType.PREFIX, RankEditorMenu.this).start();
        }
    }

    private class EditSuffixButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.NAME_TAG);
            builder.name("&9&lEdit Suffix");

            List<String> lore = new ArrayList<>();
            lore.add("&7Current: " + (rank.getSuffix() == null || rank.getSuffix().isEmpty() ? "&cNone" : "Player" + rank.getSuffix()));
            lore.add("");
            lore.add("&aClick to edit!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new RankInputHandler(player, rank, RankInputHandler.InputType.SUFFIX, RankEditorMenu.this).start();
        }
    }

    @SuppressWarnings("deprecation")
    private class EditColorButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ChatColor chatColor = ColorMapping.fromString(rank.getColor());
            DyeColor dyeColor = chatColor != null ? ColorMapping.chatColorToDyeColor(chatColor) : DyeColor.WHITE;

            ItemBuilder builder = new ItemBuilder(Material.WOOL);
            builder.durability(dyeColor.getWoolData());
            builder.name("&9&lEdit Color");

            String colorName = chatColor != null ? chatColor.name().replace("_", " ") : "WHITE";
            List<String> lore = new ArrayList<>();
            lore.add("&7Current: " + rank.getColor() + colorName);
            lore.add("");
            lore.add("&aClick to select!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new RankColorMenu(rank).openMenu(player);
        }
    }

    private class EditWeightButton extends Button {

        private static final int SMALL_INCREMENT = 1;
        private static final int LARGE_INCREMENT = 10;

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.ANVIL);
            builder.name("&9&lEdit Weight");

            List<String> lore = new ArrayList<>();
            lore.add("&7Current: &f" + rank.getWeight());
            lore.add("");
            lore.add("&aLeft click to increase");
            lore.add("&cRight click to decrease");
            lore.add("&7Shift click to change by 10");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            int currentWeight = rank.getWeight();
            int delta = 0;

            switch (clickType) {
                case LEFT:
                    delta = SMALL_INCREMENT;
                    break;
                case SHIFT_LEFT:
                    delta = LARGE_INCREMENT;
                    break;
                case RIGHT:
                    delta = -SMALL_INCREMENT;
                    break;
                case SHIFT_RIGHT:
                    delta = -LARGE_INCREMENT;
                    break;
                default:
                    return;
            }

            int newWeight = Math.max(0, currentWeight + delta);
            rank.setWeight(newWeight);
            ServiceContainer.getService(RankService.class).save(rank);
            LOGGER.info(player.getName() + " changed weight of rank " + rank.getName() + " to " + newWeight);
            Button.playNeutral(player);
            new RankEditorMenu(rank).openMenu(player);
        }
    }

    private class PermissionsButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.BOOK);
            builder.name("&9&lManage Permissions");

            List<String> lore = new ArrayList<>();
            lore.add("&7Total: &f" + rank.getPermissions().size());
            lore.add("");
            lore.add("&aClick to manage!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new RankPermissionsMenu(rank).openMenu(player);
        }
    }

    private class InheritancesButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.CHEST);
            builder.name("&9&lManage Inheritances");

            List<String> lore = new ArrayList<>();
            lore.add("&7Total: &f" + rank.getInheritances().size());
            lore.add("");
            if (!rank.getInheritances().isEmpty()) {
                lore.add("&7Inherits from:");
                for (String inheritance : rank.getInheritances()) {
                    Rank inheritedRank = ServiceContainer.getService(RankService.class).getRank(inheritance);
                    if (inheritedRank != null) {
                        lore.add("&f- " + inheritedRank.getColor() + inheritedRank.getName());
                    } else {
                        lore.add("&f- &c" + inheritance + " (invalid)");
                    }
                }
                lore.add("");
            }
            lore.add("&aClick to manage!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new RankInheritanceMenu(rank).openMenu(player);
        }
    }

    private class TogglePropertiesButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.LEVER);
            builder.name("&9&lToggle Properties");

            List<String> lore = new ArrayList<>();
            lore.add("&7Staff: " + (rank.isStaff() ? "&aYes" : "&cNo"));
            lore.add("&7Default: " + (rank.isDefaultRank() ? "&aYes" : "&cNo"));
            lore.add("&7Hidden: " + (rank.isHidden() ? "&aYes" : "&cNo"));
            lore.add("&7Purchasable: " + (rank.isPurchasable() ? "&aYes" : "&cNo"));
            lore.add("");
            lore.add("&aClick to toggle!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            new RankPropertiesMenu(rank).openMenu(player);
        }
    }

    private class DiscordLinkButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.NETHER_STAR);
            builder.name("&9&lDiscord Link");

            List<String> lore = new ArrayList<>();
            lore.add("&7Link this rank to a Discord role.");
            lore.add("");
            lore.add("&cComing soon!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playFail(player);
        }
    }
}
