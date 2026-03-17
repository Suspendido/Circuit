package com.sylluxpvp.circuit.bukkit.menus.tag;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.sylluxpvp.circuit.bukkit.tools.menu.Button;
import com.sylluxpvp.circuit.bukkit.tools.menu.pagination.PaginatedMenu;
import com.sylluxpvp.circuit.bukkit.tools.spigot.ItemBuilder;
import com.sylluxpvp.circuit.shared.tag.Tag;
import com.sylluxpvp.circuit.shared.service.ServiceContainer;
import com.sylluxpvp.circuit.shared.service.impl.TagService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagListMenu extends PaginatedMenu {

    private static final int MENU_SIZE = 45;

    @Override
    public int getSize() {
        return MENU_SIZE;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&9Tag Editor";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        TagService service = ServiceContainer.getService(TagService.class);

        for (Tag tag : service.getSortedTags()) {
            buttons.put(buttons.size(), new TagListButton(tag));
        }

        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(MENU_SIZE - 5, new CreateTagButton());
        return buttons;
    }

    @RequiredArgsConstructor
    private static class TagListButton extends Button {

        private final Tag tag;

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
            lore.add("");
            lore.add("&aLeft Click to edit!");
            lore.add("&cRight Click to delete!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
                Button.playNeutral(player);
                new TagDeleteConfirmMenu(tag).openMenu(player);
                return;
            }
            Button.playNeutral(player);
            new TagEditorMenu(tag).openMenu(player);
        }
    }

    private static class CreateTagButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.EMERALD);
            builder.name("&a&lCreate Tag");

            List<String> lore = new ArrayList<>();
            lore.add("&7Click to create a new tag.");
            lore.add("");
            lore.add("&aClick to create!");

            builder.lore(lore);
            return builder.build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Button.playNeutral(player);
            player.closeInventory();
            new TagInputHandler(player, null, TagInputHandler.InputType.CREATE, new TagListMenu()).start();
        }
    }
}
