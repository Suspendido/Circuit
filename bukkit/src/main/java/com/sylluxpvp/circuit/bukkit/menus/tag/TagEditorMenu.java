package com.sylluxpvp.circuit.bukkit.menus.tag;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.CircuitPlugin;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.Menu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.TagService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class TagEditorMenu extends Menu {

    private static final int MENU_SIZE = 45;
    private static final Logger LOGGER = CircuitPlugin.getInstance().getLogger();

    private final Tag tag;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getTitle(Player player) {
        return "&9Editing Tag: &f" + tag.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        fillBorder(buttons);

        buttons.put(10, new DisplayInfoButton());
        buttons.put(12, new EditDisplayButton());
        buttons.put(14, new EditPermissionButton());
        buttons.put(16, new TogglePurchasableButton());

        addBackButton(buttons, new TagListMenu());

        return buttons;
    }

    private class DisplayInfoButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.NAME_TAG);
            builder.name("&9" + tag.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&9&lTag Information");
            lore.add("&fDisplay: " + tag.getDisplay());
            lore.add("&fPermission: &9" + tag.getPermission());
            lore.add("&fPurchasable: " + (tag.isPurchasable() ? "&aYes" : "&cNo"));

            builder.lore(lore);
            return builder.build();
        }
    }

    private class EditDisplayButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.OAK_SIGN);
            builder.name("&9&lEdit Display");

            List<String> lore = new ArrayList<>();
            lore.add("&7Current: " + tag.getDisplay());
            lore.add("");
            lore.add("&aClick to edit!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new TagInputHandler(player, tag, TagInputHandler.InputType.DISPLAY, TagEditorMenu.this).start();
        }
    }

    private class EditPermissionButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.PAPER);
            builder.name("&9&lEdit Permission");

            List<String> lore = new ArrayList<>();
            lore.add("&7Current: &f" + tag.getPermission());
            lore.add("");
            lore.add("&aClick to edit!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new TagInputHandler(player, tag, TagInputHandler.InputType.PERMISSION, TagEditorMenu.this).start();
        }
    }

    private class TogglePurchasableButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.GOLD_INGOT);
            builder.name("&9&lToggle Purchasable");

            List<String> lore = new ArrayList<>();
            lore.add("&7Current: " + (tag.isPurchasable() ? "&aYes" : "&cNo"));
            lore.add("");
            lore.add("&aClick to toggle!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            tag.setPurchasable(!tag.isPurchasable());
            ServiceContainer.getService(TagService.class).save(tag);
            LOGGER.info(player.getName() + " toggled purchasable of tag " + tag.getName() + " to " + tag.isPurchasable());
            Button.playNeutral(player);
            new TagEditorMenu(tag).openMenu(player);
        }
    }
}
